import sqlite3
import os
import shutil
# Your MercyHacks keys
devDeviceId = "0fe72035-38b9-44e3-a2c1-f178c5e60dd5"
machineId = "427da15b19f1db46dfdafb646a7842d809b579fd1dc09aa979246db9a74c3dfe"
macMachineId = "6cfd1098d8ec46b2b3c36d9c67bc155fa455fdf62cc2708bc9d18a2d7cac09f72261d21a3e15191d6fe9c9e0b9303cbf932f81c10f55da364e6de75ab00ae179"
sqmId = "{4A818956-B6D1-4400-89BF-FED6D0919BFC}"
# Path to SQLite database
home_dir = os.path.expanduser("/home/k1en/")
db_path = os.path.join(home_dir, ".config/Cursor/User/globalStorage/state.vscdb")
if os.path.exists(db_path):
    # Create backup
    backup_path = db_path + ".backup"
    shutil.copy2(db_path, backup_path)
    print(f"Created backup at {backup_path}")
    # Connect to database
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    # Update values
    cursor.execute("UPDATE ItemTable SET value = ? WHERE key = 'telemetry.devDeviceId'", (devDeviceId,))
    cursor.execute("UPDATE ItemTable SET value = ? WHERE key = 'telemetry.machineId'", (machineId,))
    cursor.execute("UPDATE ItemTable SET value = ? WHERE key = 'telemetry.macMachineId'", (macMachineId,))
    cursor.execute("UPDATE ItemTable SET value = ? WHERE key = 'telemetry.sqmId'", (sqmId,))
    cursor.execute("UPDATE ItemTable SET value = ? WHERE key = 'storage.serviceMachineId'", (devDeviceId,))
    # Commit changes and close
    conn.commit()
    conn.close()
    print("Successfully updated Cursor database")
else:
    print(f"Database not found at {db_path}")

