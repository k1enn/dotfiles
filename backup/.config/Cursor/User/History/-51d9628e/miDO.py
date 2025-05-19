import os
import json
import curses
import logging
from datetime import datetime, date
from typing import Optional, List
from dataclasses import dataclass, field
from enum import Enum, auto

class Priority(Enum):
    LOW = auto()
    MEDIUM = auto()
    HIGH = auto()

@dataclass
class Task:
    """Represents a task with a name, due date, completion status, and priority."""
    name: str
    due_date: str
    completed: bool = False
    priority: Priority = Priority.MEDIUM
    tags: List[str] = field(default_factory=list)
    
    def __post_init__(self):
        """Validate task data after initialization."""
        if not self.name or len(self.name.strip()) == 0:
            raise ValueError("Task name cannot be empty")
        if len(self.name) > 100:
            raise ValueError("Task name cannot exceed 100 characters")
        try:
            datetime.strptime(self.due_date, "%Y-%m-%d")
        except ValueError:
            raise ValueError("Invalid date format. Use YYYY-MM-DD")
    
    def toggle_completion(self):
        """Toggle completion status."""
        self.completed = not self.completed
    
    def is_past(self):
        """Check if the task is in the past."""
        today = date.today().isoformat()
        return self.due_date < today
    
    def is_present(self):
        """Check if the task is for today."""
        today = date.today().isoformat()
        return self.due_date == today
    
    def is_future(self):
        """Check if the task is in the future."""
        today = date.today().isoformat()
        return self.due_date > today
    
    def to_dict(self):
        """Convert to dictionary for JSON serialization."""
        return {
            "name": self.name,
            "due_date": self.due_date,
            "completed": self.completed,
            "priority": self.priority.name,
            "tags": self.tags
        }
    
    @classmethod
    def from_dict(cls, data):
        """Create Task from dictionary."""
        return cls(
            name=data["name"],
            due_date=data["due_date"],
            completed=data["completed"],
            priority=Priority[data.get("priority", "MEDIUM")],
            tags=data.get("tags", [])
        )
    
    def add_tag(self, tag: str):
        """Add a tag to the task."""
        if tag not in self.tags:
            self.tags.append(tag)
    
    def remove_tag(self, tag: str):
        """Remove a tag from the task."""
        if tag in self.tags:
            self.tags.remove(tag)

class TodoistTUI:
    """Terminal User Interface for Todoist."""
    
    def __init__(self):
        """Initialize the application."""
        self.tasks = []
        self.current_category = 0  # 0: Past, 1: Present, 2: Future
        self.current_task_index = 0
        self.add_mode = False
        self.new_task_name = ""
        self.new_task_date = ""
        self.name_input_active = True  # True for name input, False for date input
        self.load_tasks()
    
    def load_tasks(self):
        """Load tasks from file."""
        if os.path.exists("tasks.json"):
            try:
                with open("tasks.json", "r") as f:
                    tasks_data = json.load(f)
                    self.tasks = [Task.from_dict(task) for task in tasks_data]
            except Exception as e:
                print(f"Error loading tasks: {e}")
    
    def save_tasks(self):
        """Save tasks to file."""
        try:
            with open("tasks.json", "w") as f:
                json.dump([task.to_dict() for task in self.tasks], f)
        except Exception as e:
                print(f"Error saving tasks: {e}")
    
    def get_tasks_by_category(self, category):
        """Get tasks for a specific category."""
        if category == 0:  # Past
            return [task for task in self.tasks if task.is_past()]
        elif category == 1:  # Present
            return [task for task in self.tasks if task.is_present()]
        else:  # Future
            return [task for task in self.tasks if task.is_future()]
    
    def add_task(self):
        """Add a new task."""
        if not self.new_task_name or not self.new_task_date:
            return
        
        try:
            # Validate date format
            datetime.strptime(self.new_task_date, "%Y-%m-%d")
            
            # Create and add task
            task = Task(self.new_task_name, self.new_task_date)
            self.tasks.append(task)
            
            # Reset input fields
            self.new_task_name = ""
            self.new_task_date = ""
            self.add_mode = False
            
            # Save tasks
            self.save_tasks()
        except ValueError:
            # Invalid date format
            pass
    
    def toggle_current_task(self):
        """Toggle completion status of the selected task."""
        category_tasks = self.get_tasks_by_category(self.current_category)
        if 0 <= self.current_task_index < len(category_tasks):
            category_tasks[self.current_task_index].toggle_completion()
            self.save_tasks()
    
    def run(self, stdscr):
        """Main application loop."""
        curses.curs_set(0)  # Hide cursor
        stdscr.clear()
        
        # Set up colors
        curses.start_color()
        curses.init_pair(1, curses.COLOR_WHITE, curses.COLOR_BLUE)  # Header
        curses.init_pair(2, curses.COLOR_BLACK, curses.COLOR_RED)   # Past
        curses.init_pair(3, curses.COLOR_BLACK, curses.COLOR_YELLOW)  # Present
        curses.init_pair(4, curses.COLOR_BLACK, curses.COLOR_GREEN)  # Future
        curses.init_pair(5, curses.COLOR_BLACK, curses.COLOR_WHITE)  # Selected
        
        while True:
            stdscr.clear()
            height, width = stdscr.getmaxyx()
            
            # Draw header
            header = "TODOIST TUI APP"
            stdscr.addstr(0, (width - len(header)) // 2, header, curses.color_pair(1))
            
            # Draw category tabs
            categories = ["PAST", "PRESENT", "FUTURE"]
            tab_width = width // 3
            for i, category in enumerate(categories):
                x = i * tab_width
                style = curses.A_REVERSE if i == self.current_category else curses.A_NORMAL
                stdscr.addstr(2, x + (tab_width - len(category)) // 2, category, style)
            
            # Draw horizontal line
            stdscr.addstr(3, 0, "=" * width)
            
            # Draw tasks for current category
            category_tasks = self.get_tasks_by_category(self.current_category)
            for i, task in enumerate(category_tasks):
                y = 5 + i
                if y < height - 5:  # Leave space for input area
                    status = "[✓]" if task.completed else "[ ]"
                    task_str = f"{status} {task.name} (Due: {task.due_date})"
                    
                    if i == self.current_task_index and not self.add_mode:
                        stdscr.addstr(y, 2, task_str, curses.color_pair(5))
                    else:
                        stdscr.addstr(y, 2, task_str)
            
            # Draw input area if in add mode
            if self.add_mode:
                stdscr.addstr(height - 4, 2, "New Task Name: ")
                stdscr.addstr(height - 4, 16, self.new_task_name)
                
                stdscr.addstr(height - 3, 2, "Due Date (YYYY-MM-DD): ")
                stdscr.addstr(height - 3, 25, self.new_task_date)
                
                # Show cursor at appropriate position
                if self.name_input_active:
                    curses.curs_set(1)  # Show cursor
                    stdscr.move(height - 4, 16 + len(self.new_task_name))
                else:
                    curses.curs_set(1)  # Show cursor
                    stdscr.move(height - 3, 25 + len(self.new_task_date))
            else:
                curses.curs_set(0)  # Hide cursor
            
            # Draw footer
            footer = "q: Quit | a: Add Task | Space: Toggle Completion | Tab: Switch Category"
            if self.add_mode:
                footer = "Enter: Save Task | Esc: Cancel | Tab: Switch Field"
            stdscr.addstr(height - 1, 0, footer, curses.A_REVERSE)
            
            # Refresh screen
            stdscr.refresh()
            
            # Handle key input
            key = stdscr.getch()
            
            if self.add_mode:
                if key == 27:  # Escape key
                    self.add_mode = False
                    self.new_task_name = ""
                    self.new_task_date = ""
                elif key == 9:  # Tab key
                    self.name_input_active = not self.name_input_active
                elif key == 10:  # Enter key
                    self.add_task()
                elif key == curses.KEY_BACKSPACE or key == 127:  # Backspace
                    if self.name_input_active:
                        self.new_task_name = self.new_task_name[:-1]
                    else:
                        self.new_task_date = self.new_task_date[:-1]
                elif 32 <= key <= 126:  # Printable characters
                    if self.name_input_active:
                        self.new_task_name += chr(key)
                    else:
                        self.new_task_date += chr(key)
            else:
                if key == ord('q'):
                    break
                elif key == ord('a'):
                    self.add_mode = True
                    self.name_input_active = True
                elif key == 9:  # Tab key
                    self.current_category = (self.current_category + 1) % 3
                    self.current_task_index = 0
                elif key == 32:  # Space key
                    self.toggle_current_task()
                elif key == curses.KEY_UP:
                    self.current_task_index = max(0, self.current_task_index - 1)
                elif key == curses.KEY_DOWN:
                    category_tasks = self.get_tasks_by_category(self.current_category)
                    self.current_task_index = min(len(category_tasks) - 1, 
                                               self.current_task_index + 1) if category_tasks else 0
        
        # Save tasks before exiting
        self.save_tasks()

def main():
    """Initialize and run the Todoist TUI application."""
    app = TodoistTUI()
    curses.wrapper(app.run)

if __name__ == "__main__":
    main()

