#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <omp.h>

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

int main() {
    srand(time(NULL));
    char **a = (char **)malloc(ROWS * sizeof(char *));
    for (int i = 0; i < ROWS; i++)
        a[i] = (char *)malloc(COLS * sizeof(char));

    // Generate random matrix
    for (int i = 0; i < ROWS; i++)
        for (int j = 0; j < COLS; j++)
            a[i][j] = (rand() % 26) + 'a'; // Lowercase a-z

    int thread_counts[] = {1, 2, 3, 4, 5, 6, 7, 8};
    int num_thread_configs = 8;

    for (int t = 0; t < num_thread_configs; t++) {
        int num_threads = thread_counts[t];
        omp_set_num_threads(num_threads);

        for (int len = MIN_LEN; len <= MAX_LEN; len++) {
            long count = 0;
            double start_time = omp_get_wtime();

            // Horizontal search
            #pragma omp parallel for schedule(dynamic) reduction(+:count)
            for (int i = 0; i < ROWS; i++) {
                char str[MAX_LEN + 1];
                for (int j = 0; j <= COLS - len; j++) {
                    for (int k = 0; k < len; k++)
                        str[k] = a[i][j + k];
                    str[len] = '\0';
                    if (is_palindrome(str, len))
                        count++;
                }
            }

            // Vertical search
            #pragma omp parallel for schedule(dynamic) reduction(+:count)
            for (int j = 0; j < COLS; j++) {
                char str[MAX_LEN + 1];
                for (int i = 0; i <= ROWS - len; i++) {
                    for (int k = 0; k < len; k++)
                        str[k] = a[i + k][j];
                    str[len] = '\0';
                    if (is_palindrome(str, len))
                        count++;
                }
            }

            // Diagonal (down-right) search
            #pragma omp parallel for schedule(dynamic) reduction(+:count)
            for (int i = 0; i <= ROWS - len; i++) {
                char str[MAX_LEN + 1];
                for (int j = 0; j <= COLS - len; j++) {
                    for (int k = 0; k < len; k++)
                        str[k] = a[i + k][j + k];
                    str[len] = '\0';
                    if (is_palindrome(str, len))
                        count++;
                }
            }

            double end_time = omp_get_wtime();
            printf("%ld palindromes of size %d found in %f s. using %d threads.\n",
                   count, len, end_time - start_time, num_threads);
        }
        printf("********************************************************************************\n");
    }

    // Free memory
    for (int i = 0; i < ROWS; i++)
        free(a[i]);
    free(a);
    return 0;
}