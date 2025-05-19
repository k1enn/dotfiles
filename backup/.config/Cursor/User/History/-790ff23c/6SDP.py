#!/usr/bin/env python3
"""
Simple test script to verify that task saving works correctly.
"""

import os
import sys
import json
from datetime import date

class SimpleTaskTest:
    def __init__(self):
        self.data_file = "test_tasks.json"
        self.tasks = []
    
    def add_task(self, name, due_date=None):
        """Add a simple task and save it."""
        if due_date is None:
            due_date = date.today().isoformat()
        
        task = {
            "name": name,
            "due_date": due_date,
            "completed": False,
            "priority": "MEDIUM",
            "tags": []
        }
        
        self.tasks.append(task)
        print(f"Added task: {name}")
        return self.save_tasks()
    
    def save_tasks(self):
        """Save tasks to file and return success/failure."""
        try:
            with open(self.data_file, "w") as f:
                json.dump(self.tasks, f, indent=2)
            print(f"Tasks saved to {self.data_file}")
            return True
        except Exception as e:
            print(f"Error saving tasks: {e}")
            return False
    
    def load_tasks(self):
        """Load tasks from file and return success/failure."""
        try:
            if os.path.exists(self.data_file):
                with open(self.data_file, "r") as f:
                    self.tasks = json.load(f)
                print(f"Loaded {len(self.tasks)} tasks from {self.data_file}")
            else:
                print(f"No tasks file found at {self.data_file}")
            return True
        except Exception as e:
            print(f"Error loading tasks: {e}")
            return False
    
    def list_tasks(self):
        """Print tasks to console."""
        if not self.tasks:
            print("No tasks found.")
            return
        
        print("\nCurrent Tasks:")
        print("-" * 50)
        for i, task in enumerate(self.tasks, 1):
            status = "✓" if task["completed"] else " "
            print(f"{i}. [{status}] {task['name']} (Due: {task['due_date']})")
        print("-" * 50)

def run_test():
    """Run a simple test of adding and saving tasks."""
    print("\n=== TODOIST SIMPLE TEST ===\n")
    
    test = SimpleTaskTest()
    
    # Try to load existing tasks
    test.load_tasks()
    test.list_tasks()
    
    # Add a new task
    task_name = f"Test task created on {date.today().isoformat()}"
    if test.add_task(task_name):
        print("Task added successfully!")
    else:
        print("Failed to add task!")
    
    # List updated tasks
    test.list_tasks()
    
    print("\nTest completed!")

if __name__ == "__main__":
    run_test() 