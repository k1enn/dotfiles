import os
import json
import curses
import logging
import sys
import time
from datetime import datetime, date, timedelta
from typing import Optional, List, Tuple, Dict
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

class NotificationType(Enum):
    INFO = 1
    SUCCESS = 2
    WARNING = 3
    ERROR = 4

@dataclass
class Notification:
    """Represents a notification message."""
    message: str
    type: NotificationType
    duration: int = 3  # seconds
    created_at: float = field(default_factory=time.time)
    
    def is_expired(self):
        """Check if notification has expired."""
        return time.time() - self.created_at > self.duration

class TodoistTUI:
    """Terminal User Interface for Todoist."""
    
    def __init__(self):
        """Initialize the application."""
        self.tasks = []
        self.current_category = 0  # 0: Past, 1: Present, 2: Future
        self.current_task_index = 0
        self.add_mode = False
        self.edit_mode = False
        self.new_task_name = ""
        self.new_task_date = ""
        self.name_input_active = True
        self.notifications = []
        self.error_message = ""
        self.error_timer = 0
        
        # Initialize app
        self.config = self.load_config()
        self.setup_logging()
        self.load_tasks()
        
        # Add initial notifications after everything is set up
        self.add_notification("Todoist TUI started", NotificationType.INFO)
        if os.path.exists("config.json"):
            self.add_notification("Configuration loaded", NotificationType.INFO)
    
    def setup_logging(self):
        """Set up logging configuration."""
        try:
            log_dir = os.path.dirname("todoist.log")
            if log_dir and not os.path.exists(log_dir):
                os.makedirs(log_dir)
                
            logging.basicConfig(
                filename='todoist.log',
                level=logging.INFO,
                format='%(asctime)s - %(levelname)s - %(message)s'
            )
        except (OSError, IOError) as e:
            # If we can't write to the log file, set up console logging instead
            print(f"Warning: Could not set up file logging: {e}")
            logging.basicConfig(
                level=logging.INFO,
                format='%(asctime)s - %(levelname)s - %(message)s'
            )
    
    def load_config(self):
        """Load configuration from file."""
        config = {
            "data_file": "tasks.json",
            "auto_save": True,
            "page_size": 10,
            "colors": {
                "header": (curses.COLOR_WHITE, curses.COLOR_BLUE),
                "past": (curses.COLOR_BLACK, curses.COLOR_RED),
                "present": (curses.COLOR_BLACK, curses.COLOR_YELLOW),
                "future": (curses.COLOR_BLACK, curses.COLOR_GREEN),
                "selected": (curses.COLOR_BLACK, curses.COLOR_WHITE),
                "error": (curses.COLOR_WHITE, curses.COLOR_RED),
                "success": (curses.COLOR_BLACK, curses.COLOR_GREEN),
                "info": (curses.COLOR_BLACK, curses.COLOR_CYAN),
                "warning": (curses.COLOR_BLACK, curses.COLOR_YELLOW)
            }
        }
        try:
            if os.path.exists("config.json"):
                with open("config.json", "r") as f:
                    loaded_config = json.load(f)
                    # Merge colors without overwriting the entire colors dict
                    if "colors" in loaded_config:
                        for k, v in loaded_config["colors"].items():
                            config["colors"][k] = tuple(v)
                        del loaded_config["colors"]
                    config.update(loaded_config)
                # Don't add notification here since notifications aren't initialized yet
        except Exception as e:
            logging.error(f"Error loading config: {e}")
            # Don't add notification here since notifications aren't initialized yet
        return config
    
    def initialize_data_directory(self):
        """Create data directory if it doesn't exist."""
        try:
            data_dir = os.path.dirname(self.config["data_file"])
            if data_dir and not os.path.exists(data_dir):
                os.makedirs(data_dir)
                logging.info(f"Created data directory: {data_dir}")
        except OSError as e:
            logging.error(f"Error creating data directory: {e}")
            self.add_notification(f"Error creating directory: {e}", NotificationType.ERROR)
    
    def load_tasks(self):
        """Load tasks from file."""
        try:
            if os.path.exists(self.config["data_file"]):
                with open(self.config["data_file"], "r") as f:
                    try:
                        tasks_data = json.load(f)
                        self.tasks = [Task.from_dict(task) for task in tasks_data]
                        logging.info(f"Successfully loaded {len(self.tasks)} tasks")
                        self.add_notification(f"Loaded {len(self.tasks)} tasks", NotificationType.INFO)
                    except json.JSONDecodeError:
                        logging.error("Invalid JSON in tasks file")
                        self.add_notification("Tasks file is corrupted", NotificationType.ERROR)
                        # Create backup of corrupted file
                        backup_file = f"{self.config['data_file']}.bak"
                        try:
                            import shutil
                            shutil.copy2(self.config["data_file"], backup_file)
                            self.add_notification(f"Created backup at {backup_file}", NotificationType.INFO)
                        except Exception as e:
                            logging.error(f"Could not create backup: {e}")
            else:
                self.initialize_data_directory()
                # Create empty tasks file
                self.save_tasks()
        except Exception as e:
            logging.error(f"Error loading tasks: {e}")
            self.add_notification(f"Error loading tasks: {e}", NotificationType.ERROR)
            self.tasks = []  # Start with empty task list
    
    def save_tasks(self):
        """Save tasks to file."""
        if not self.config["auto_save"]:
            return
            
        try:
            # Ensure the directory exists
            self.initialize_data_directory()
            
            # Create temporary file first to avoid corrupting the existing file
            temp_file = f"{self.config['data_file']}.tmp"
            with open(temp_file, "w") as f:
                json.dump([task.to_dict() for task in self.tasks], f, indent=2)
            
            # Replace the actual file with the temporary file
            import os
            if os.path.exists(self.config["data_file"]):
                os.replace(temp_file, self.config["data_file"])
            else:
                os.rename(temp_file, self.config["data_file"])
                
            logging.info("Tasks saved successfully")
            self.add_notification("Tasks saved successfully", NotificationType.SUCCESS)
        except Exception as e:
            logging.error(f"Error saving tasks: {e}")
            self.add_notification(f"Error saving tasks: {e}", NotificationType.ERROR)
    
    def add_notification(self, message, notification_type):
        """Add a notification to be displayed."""
        self.notifications.append(Notification(message, notification_type))
    
    def show_error(self, message):
        """Display error message to user."""
        self.error_message = message
        self.error_timer = 30  # Display for 30 iterations of the main loop
        self.add_notification(message, NotificationType.ERROR)
    
    def get_tasks_by_category(self, category):
        """Get tasks for a specific category."""
        if category == 0:  # Past
            tasks = [task for task in self.tasks if task.is_past()]
        elif category == 1:  # Present
            tasks = [task for task in self.tasks if task.is_present()]
        else:  # Future
            tasks = [task for task in self.tasks if task.is_future()]
        
        # Sort by priority and due date
        return sorted(tasks, key=lambda x: (x.priority.value, x.due_date))
    
    def add_task(self):
        """Add a new task."""
        if not self.new_task_name or not self.new_task_date:
            self.show_error("Task name and date are required")
            return
        
        try:
            task = Task(self.new_task_name, self.new_task_date)
            self.tasks.append(task)
            self.new_task_name = ""
            self.new_task_date = ""
            self.add_mode = False
            self.save_tasks()
            self.add_notification(f"Added task: {task.name}", NotificationType.SUCCESS)
            logging.info(f"Added new task: {task.name}")
        except ValueError as e:
            self.show_error(str(e))
        except Exception as e:
            logging.error(f"Error adding task: {e}")
            self.show_error("Error adding task. Check todoist.log for details.")
    
    def increase_date_by_days(self, days=1):
        """Increase the date in the date field by a given number of days."""
        try:
            if self.new_task_date:
                current_date = datetime.strptime(self.new_task_date, "%Y-%m-%d").date()
                new_date = current_date + timedelta(days=days)
                self.new_task_date = new_date.isoformat()
        except ValueError:
            # If current date is invalid, just use today
            self.new_task_date = date.today().isoformat()
    
    def decrease_date_by_days(self, days=1):
        """Decrease the date in the date field by a given number of days."""
        try:
            if self.new_task_date:
                current_date = datetime.strptime(self.new_task_date, "%Y-%m-%d").date()
                new_date = current_date - timedelta(days=days)
                self.new_task_date = new_date.isoformat()
        except ValueError:
            # If current date is invalid, just use today
            self.new_task_date = date.today().isoformat()
    
    def set_date_to_today(self):
        """Set the date to today."""
        self.new_task_date = date.today().isoformat()
    
    def add_one_month(self):
        """Add one month to the date."""
        try:
            if self.new_task_date:
                current_date = datetime.strptime(self.new_task_date, "%Y-%m-%d").date()
                # Add a month (roughly)
                year = current_date.year + ((current_date.month + 1) // 13)
                month = ((current_date.month) % 12) + 1
                day = min(current_date.day, [31, 29 if year % 4 == 0 and (year % 100 != 0 or year % 400 == 0) else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][month-1])
                new_date = date(year, month, day)
                self.new_task_date = new_date.isoformat()
        except ValueError:
            # If current date is invalid, just use today
            self.new_task_date = date.today().isoformat()
    
    def delete_current_task(self):
        """Delete the currently selected task."""
        category_tasks = self.get_tasks_by_category(self.current_category)
        if 0 <= self.current_task_index < len(category_tasks):
            task = category_tasks[self.current_task_index]
            task_name = task.name
            self.tasks.remove(task)
            self.save_tasks()
            self.add_notification(f"Deleted task: {task_name}", NotificationType.INFO)
            logging.info(f"Deleted task: {task_name}")
    
    def edit_current_task(self):
        """Edit the currently selected task."""
        category_tasks = self.get_tasks_by_category(self.current_category)
        if 0 <= self.current_task_index < len(category_tasks):
            task = category_tasks[self.current_task_index]
            self.edit_mode = True
            self.new_task_name = task.name
            self.new_task_date = task.due_date
            self.name_input_active = True
    
    def save_edited_task(self):
        """Save the edited task."""
        category_tasks = self.get_tasks_by_category(self.current_category)
        if 0 <= self.current_task_index < len(category_tasks):
            old_task = category_tasks[self.current_task_index]
            try:
                new_task = Task(self.new_task_name, self.new_task_date, 
                               old_task.completed, old_task.priority, old_task.tags)
                index = self.tasks.index(old_task)
                self.tasks[index] = new_task
                self.edit_mode = False
                self.new_task_name = ""
                self.new_task_date = ""
                self.save_tasks()
                self.add_notification(f"Updated task: {new_task.name}", NotificationType.SUCCESS)
                logging.info(f"Edited task: {old_task.name} -> {new_task.name}")
            except ValueError as e:
                self.show_error(str(e))
            except Exception as e:
                logging.error(f"Error editing task: {e}")
                self.show_error("Error editing task. Check todoist.log for details.")
    
    def toggle_current_task(self):
        """Toggle completion status of the selected task."""
        category_tasks = self.get_tasks_by_category(self.current_category)
        if 0 <= self.current_task_index < len(category_tasks):
            task = category_tasks[self.current_task_index]
            task.toggle_completion()
            status = "completed" if task.completed else "reopened"
            self.add_notification(f"Task {status}: {task.name}", NotificationType.INFO)
            self.save_tasks()
    
    def process_notifications(self):
        """Remove expired notifications."""
        self.notifications = [n for n in self.notifications if not n.is_expired()]
    
    def run(self, stdscr):
        """Main application loop."""
        curses.curs_set(0)  # Hide cursor
        stdscr.clear()
        
        # Set up colors
        curses.start_color()
        curses.use_default_colors()  # Allow terminal default colors
        
        # Initialize color pairs
        for i, (fg, bg) in enumerate(self.config["colors"].values(), 1):
            curses.init_pair(i, fg, bg)
        
        # Map notification types to color pairs
        notification_colors = {
            NotificationType.INFO: curses.color_pair(8),
            NotificationType.SUCCESS: curses.color_pair(7),
            NotificationType.WARNING: curses.color_pair(9),
            NotificationType.ERROR: curses.color_pair(6)
        }
        
        while True:
            # Process and clean up expired notifications
            self.process_notifications()
            
            stdscr.clear()
            height, width = stdscr.getmaxyx()
            
            # Draw header
            header = "TODOIST TUI APP"
            stdscr.addstr(0, (width - len(header)) // 2, header, curses.color_pair(1))
            
            # Draw notifications if any
            if self.notifications:
                y = 1
                for i, notification in enumerate(self.notifications[:3]):  # Show at most 3 notifications
                    if y < height - 1:
                        color = notification_colors.get(notification.type, 0)
                        remaining = int(notification.duration - (time.time() - notification.created_at))
                        message = f"{notification.message} [{remaining}s]"
                        try:
                            stdscr.addstr(y, 2, message, color)
                        except curses.error:
                            # Handle potential display errors
                            pass
                        y += 1
            
            # Draw category tabs
            categories = ["PAST", "PRESENT", "FUTURE"]
            tab_width = width // 3
            for i, category in enumerate(categories):
                x = i * tab_width
                style = curses.A_REVERSE if i == self.current_category else curses.A_NORMAL
                try:
                    stdscr.addstr(2, x + (tab_width - len(category)) // 2, category, style)
                except curses.error:
                    # Handle potential display errors
                    pass
            
            # Draw horizontal line
            try:
                stdscr.addstr(3, 0, "=" * (width - 1))
            except curses.error:
                # Handle potential display errors
                pass
            
            # Draw tasks for current category with pagination
            category_tasks = self.get_tasks_by_category(self.current_category)
            page_size = self.config["page_size"]
            current_page = self.current_task_index // page_size
            start_idx = current_page * page_size
            end_idx = min(start_idx + page_size, len(category_tasks))
            
            # Task display area
            for i, task in enumerate(category_tasks[start_idx:end_idx], start=start_idx):
                y = 5 + (i - start_idx)
                if y < height - 5:  # Leave space for input area
                    status = "[✓]" if task.completed else "[ ]"
                    priority_icons = {
                        Priority.LOW: "⌄",
                        Priority.MEDIUM: "⚬",
                        Priority.HIGH: "⌃"
                    }
                    prio_icon = priority_icons.get(task.priority, "")
                    tags = " #" + " #".join(task.tags) if task.tags else ""
                    
                    # Format task string
                    task_str = f"{status} {prio_icon} {task.name} (Due: {task.due_date}){tags}"
                    
                    # Calculate color
                    color = curses.color_pair(0)
                    if task.is_past():
                        color = curses.color_pair(2)  # past color
                    elif task.is_present():
                        color = curses.color_pair(3)  # present color
                    else:
                        color = curses.color_pair(4)  # future color
                    
                    # If selected, use selected color
                    if i == self.current_task_index and not (self.add_mode or self.edit_mode):
                        color = curses.color_pair(5)  # selected color
                    
                    # Truncate if too long
                    if len(task_str) > width - 4:
                        task_str = task_str[:width - 7] + "..."
                    
                    try:
                        stdscr.addstr(y, 2, task_str, color)
                    except curses.error:
                        # Handle potential display errors
                        pass
            
            # Show pagination info if needed
            if len(category_tasks) > page_size:
                pagination_text = f"Page {current_page + 1}/{(len(category_tasks) + page_size - 1) // page_size}"
                try:
                    stdscr.addstr(height - 5, width - len(pagination_text) - 3, pagination_text)
                except curses.error:
                    # Handle potential display errors
                    pass
            
            # Draw input area if in add/edit mode
            if self.add_mode or self.edit_mode:
                mode = "Edit" if self.edit_mode else "Add"
                try:
                    stdscr.addstr(height - 4, 2, f"{mode} Task Name: ")
                    stdscr.addstr(height - 4, 16, self.new_task_name)
                    
                    stdscr.addstr(height - 3, 2, "Due Date (YYYY-MM-DD): ")
                    stdscr.addstr(height - 3, 25, self.new_task_date)
                    
                    # Show date modification help text when in date field
                    if not self.name_input_active:
                        date_help = "+/-: Day | </> or ,/.: Week | m: Month | t: Today"
                        stdscr.addstr(height - 2, 2, date_help)
                except curses.error:
                    # Handle potential display errors
                    pass
                
                # Show cursor at appropriate position
                if self.name_input_active:
                    curses.curs_set(1)
                    try:
                        stdscr.move(height - 4, 16 + len(self.new_task_name))
                    except curses.error:
                        pass
                else:
                    curses.curs_set(1)
                    try:
                        stdscr.move(height - 3, 25 + len(self.new_task_date))
                    except curses.error:
                        pass
            else:
                curses.curs_set(0)
            
            # Draw footer with key bindings
            footer = "q: Quit | a: Add | e: Edit | d: Delete | Space: Toggle | Tab: Switch Category"
            if self.add_mode or self.edit_mode:
                footer = "Enter: Save | Esc: Cancel | Tab: Switch Field"
            
            try:
                stdscr.addstr(height - 1, 0, footer[:width-1], curses.A_REVERSE)
            except curses.error:
                # Handle potential display errors
                pass
            
            # Refresh screen
            stdscr.refresh()
            
            # Handle key input
            try:
                key = stdscr.getch()
            except curses.error:
                continue
            
            if self.add_mode or self.edit_mode:
                if key == 27:  # Escape key
                    self.add_mode = False
                    self.edit_mode = False
                    self.new_task_name = ""
                    self.new_task_date = ""
                elif key == 9:  # Tab key
                    self.name_input_active = not self.name_input_active
                elif key == 10:  # Enter key
                    if self.edit_mode:
                        self.save_edited_task()
                    else:
                        self.add_task()
                elif key == curses.KEY_BACKSPACE or key == 127 or key == 8:  # Backspace variants
                    if self.name_input_active:
                        self.new_task_name = self.new_task_name[:-1]
                    else:
                        self.new_task_date = self.new_task_date[:-1]
                elif not self.name_input_active:  # Date field is active
                    if key == ord('+'):  # Add 1 day
                        self.increase_date_by_days(1)
                    elif key == ord('-'):  # Subtract 1 day
                        self.decrease_date_by_days(1)
                    elif key == ord('>') or key == ord('.'):  # Add 1 week
                        self.increase_date_by_days(7)
                    elif key == ord('<') or key == ord(','):  # Subtract 1 week
                        self.decrease_date_by_days(7)
                    elif key == ord('m'):  # Add 1 month
                        self.add_one_month()
                    elif key == ord('t'):  # Set to today
                        self.set_date_to_today()
                    elif 32 <= key <= 126:  # Printable characters
                        if len(self.new_task_date) < 10:  # YYYY-MM-DD = 10 chars
                            self.new_task_date += chr(key)
                elif 32 <= key <= 126:  # Printable characters for name field
                    if self.name_input_active:
                        if len(self.new_task_name) < 100:  # Enforce max length
                            self.new_task_name += chr(key)
            else:
                if key == ord('q'):
                    break
                elif key == ord('a'):
                    # Default to today's date for convenience
                    self.add_mode = True
                    self.new_task_date = date.today().isoformat()
                    self.name_input_active = True
                elif key == ord('e'):
                    self.edit_current_task()
                elif key == ord('d'):
                    self.delete_current_task()
                elif key == ord('r'):  # Refresh tasks
                    self.load_tasks()
                elif key == 9:  # Tab key
                    self.current_category = (self.current_category + 1) % 3
                    self.current_task_index = 0
                elif key == 32:  # Space key
                    self.toggle_current_task()
                elif key == curses.KEY_UP:
                    self.current_task_index = max(0, self.current_task_index - 1)
                elif key == curses.KEY_DOWN:
                    self.current_task_index = min(len(category_tasks) - 1, 
                                               self.current_task_index + 1) if category_tasks else 0
                elif key == curses.KEY_PPAGE:  # Page Up
                    self.current_task_index = max(0, self.current_task_index - page_size)
                elif key == curses.KEY_NPAGE:  # Page Down
                    self.current_task_index = min(len(category_tasks) - 1,
                                               self.current_task_index + page_size) if category_tasks else 0
            
            # Short delay to manage refresh rate and CPU usage
            time.sleep(0.01)
        
        # Save tasks before exiting
        self.save_tasks()

def create_config_file():
    """Create default configuration file if it doesn't exist."""
    if not os.path.exists("config.json"):
        default_config = {
            "data_file": "tasks.json",
            "auto_save": True,
            "page_size": 10
        }
        
        try:
            with open("config.json", "w") as f:
                json.dump(default_config, f, indent=2)
            print("Created default configuration file: config.json")
        except Exception as e:
            print(f"Error creating configuration file: {e}")

def main():
    """Initialize and run the Todoist TUI application."""
    try:
        # Create default config if needed
        create_config_file()
        
        # Run the application
        app = TodoistTUI()
        curses.wrapper(app.run)
    except Exception as e:
        logging.error(f"Fatal error: {e}")
        print(f"An error occurred. Check todoist.log for details.")
        sys.exit(1)

if __name__ == "__main__":
    main()

