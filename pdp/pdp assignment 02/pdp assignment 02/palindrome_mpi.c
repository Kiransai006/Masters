#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <mpi.h>

#define ROWS 1000
#define COLS 1000
#define MIN_LEN 3
#define MAX_LEN 6

// Check if a string is a palindrome
int is_palindrome(char *str, int len) {
    for (int i = 0; i < len / 2; i++) {
        if (str[i] != str[len - 1 - i]) return 0;
    }
    return 1;
}

int main(int argc, char *argv[]) {
    MPI_Init(&argc, &argv);
    int rank, size;
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &size);

    srand(time(NULL) + rank); // Unique seed per process
    char **a = (char **)malloc(ROWS * sizeof(char *));
    for (int i = 0; i < ROWS; i++)
        a[i] = (char *)malloc(COLS * sizeof(char));

    // Generate random matrix (each process generates the same matrix)
    for (int i = 0; i < ROWS; i++)
        for (int j = 0; j < COLS; j++)
            a[i][j] = (rand() % 26) + 'a';

    int process_counts[] = {1, 2, 3, 4, 5, 6, 7, 8};
    int num_configs = 8;

    for (int t = 0; t < num_configs; t++) {
        int num_procs = process_counts[t];
        if (size != num_procs) continue; // Run only with matching process count

        for (int len = MIN_LEN; len <= MAX_LEN; len++) {
            long local_count = 0, global_count = 0;
            double start_time = MPI_Wtime();

            // Horizontal search
            for (int i = rank; i < ROWS; i += size) {
                char str[MAX_LEN + 1];
                for (int j = 0; j <= COLS - len; j++) {
                    for (int k = 0; k < len; k++)
                        str[k] = a[i][j + k];
                    str[len] = '\0';
                    if (is_palindrome(str, len))
                        local_count++;
                }
            }

            // Vertical search
            for (int j = rank; j < COLS; j += size) {
                char str[MAX_LEN + 1];
                for (int i = 0; i <= ROWS - len; i++) {
                    for (int k = 0; k < len; k++)
                        str[k] = a[i + k][j];
                    str[len] = '\0';
                    if (is_palindrome(str, len))
                        local_count++;
                }
            }

            // Diagonal (down-right) search
            for (int i = rank; i <= ROWS - len; i += size) {
                char str[MAX_LEN + 1];
                for (int j = 0; j <= COLS - len; j++) {
                    for (int k = 0; k < len; k++)
                        str[k] = a[i + k][j + k];
                    str[len] = '\0';
                    if (is_palindrome(str, len))
                        local_count++;
                }
            }

            MPI_Reduce(&local_count, &global_count, 1, MPI_LONG, MPI_SUM, 0, MPI_COMM_WORLD);
            double end_time = MPI_Wtime();

            if (rank == 0) {
                printf("%ld palindromes of size %d found in %f s. using %d processes.\n",
                       global_count, len, end_time - start_time, size);
            }
        }
        if (rank == 0) {
            printf("********************************************************************************\n");
        }
    }

    // Free memory
    for (int i = 0; i < ROWS; i++)
        free(a[i]);
    free(a);

    MPI_Finalize();
    return 0;
}