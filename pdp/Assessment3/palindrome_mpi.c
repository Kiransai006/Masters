// AdminDashboard.js
import React from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Table, TableHead, TableRow, TableCell, TableBody } from "@/components/ui/table";
import { Textarea } from "@/components/ui/textarea";
import { Plus, Mic, Timer, Star, Languages } from "lucide-react";

export default function AdminDashboard() {
  return (
    <div className="p-6 space-y-6">
      <h1 className="text-2xl font-bold">Admin Dashboard</h1>
      <Tabs defaultValue="recipes" className="space-y-4">
        <TabsList>
          <TabsTrigger value="recipes">Recipes</TabsTrigger>
          <TabsTrigger value="users">Users</TabsTrigger>
          <TabsTrigger value="dietary">Dietary Settings</TabsTrigger>
          <TabsTrigger value="utilities">Cooking Utilities</TabsTrigger>
          <TabsTrigger value="system">System Settings</TabsTrigger>
        </TabsList>

        {/* Recipes Management */}
        <TabsContent value="recipes">
          <Card>
            <CardContent className="p-4">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-lg font-semibold">Manage Recipes</h2>
                <Button variant="outline"><Plus className="mr-2 h-4 w-4" />Add Recipe</Button>
              </div>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Title</TableCell>
                    <TableCell>Diet</TableCell>
                    <TableCell>Ingredients</TableCell>
                    <TableCell>Time</TableCell>
                    <TableCell>Status</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  <TableRow>
                    <TableCell>Spicy Chickpea Bowl</TableCell>
                    <TableCell>Vegan</TableCell>
                    <TableCell>Chickpeas, Garlic</TableCell>
                    <TableCell>30 mins</TableCell>
                    <TableCell>Published</TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </TabsContent>

        {/* User Management */}
        <TabsContent value="users">
          <Card>
            <CardContent className="p-4">
              <h2 className="text-lg font-semibold mb-4">User Profiles</h2>
              <Input placeholder="Search users..." className="mb-4" />
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Name</TableCell>
                    <TableCell>Email</TableCell>
                    <TableCell>Preferences</TableCell>
                    <TableCell>Saved Recipes</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  <TableRow>
                    <TableCell>Jane Doe</TableCell>
                    <TableCell>jane@example.com</TableCell>
                    <TableCell>Gluten-Free</TableCell>
                    <TableCell>12</TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Dietary Preferences */}
        <TabsContent value="dietary">
          <Card>
            <CardContent className="p-4">
              <h2 className="text-lg font-semibold mb-4">Dietary Categories</h2>
              <ul className="space-y-2">
                {["Vegan", "Vegetarian", "Gluten-Free", "Keto", "Paleo"].map((diet) => (
                  <li key={diet} className="flex justify-between border p-2 rounded">
                    <span>{diet}</span>
                    <Button variant="ghost" size="sm">Edit</Button>
                  </li>
                ))}
              </ul>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Cooking Utilities */}
        <TabsContent value="utilities">
          <Card>
            <CardContent className="p-4 space-y-4">
              <h2 className="text-lg font-semibold">Cooking Support Features</h2>
              <div className="flex items-center justify-between">
                <span><Mic className="inline mr-2" />Voice-guided Cooking</span>
                <Button variant="outline">Enable</Button>
              </div>
              <div className="flex items-center justify-between">
                <span><Timer className="inline mr-2" />Cooking Timers</span>
                <Button variant="outline">Configure</Button>
              </div>
              <div className="flex items-center justify-between">
                <span><Star className="inline mr-2" />Skill-Level Tutorials</span>
                <Button variant="outline">Add</Button>
              </div>
              <div className="flex items-center justify-between">
                <span><Languages className="inline mr-2" />Multi-Language Support</span>
                <Button variant="outline">Settings</Button>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* System Settings */}
        <TabsContent value="system">
          <Card>
            <CardContent className="p-4 space-y-4">
              <h2 className="text-lg font-semibold">System Configuration</h2>
              <div className="flex justify-between">
                <span>Enable AI Recipe Suggestions</span>
                <Button variant="outline" size="sm">Toggle</Button>
              </div>
              <div className="flex justify-between">
                <span>Allow Offline Mode</span>
                <Button variant="outline" size="sm">Toggle</Button>
              </div>
              <div className="flex justify-between">
                <span>Enable Grocery List Export</span>
                <Button variant="outline" size="sm">Enable</Button>
              </div>
              <div className="flex justify-between">
                <span>User Authentication Config</span>
                <Button variant="outline" size="sm">Configure</Button>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}