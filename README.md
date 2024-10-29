# BlackParticle

![image](https://github.com/user-attachments/assets/77d9ae39-c376-4464-9495-317c56238d54)
<br>Figure 1. Image of an open program

**BlackParticle** is a versatile text editor written in Java that features syntax highlighting, a project file tree, and a command console. It provides a user-friendly interface for developers and writers alike, allowing easy navigation through project files and the execution of shell commands directly from the editor.

## Features

- **Syntax Highlighting**: Supports various programming languages with customizable syntax highlighting.
- **Project File Tree**: Navigate through project files easily with an integrated tree view.
- **Command Console**: Execute system commands directly from the editor and view the output in a console area.
- **Dark Theme**: Offers a modern dark theme for improved visual comfort during coding sessions.
- **File Management**: Open, save, and manage files easily with built-in file dialogs.
- **Keyboard Shortcuts**: Supports shortcuts for common actions, such as saving files (Ctrl + S).

## Installation

To run BlackParticle, you'll need to have the following prerequisites:

- **Java Development Kit (JDK)**: Ensure that JDK 8 or higher is installed on your machine.
- **Maven**: For managing dependencies.

### Steps to Install

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/tailogs/BlackParticle.git
   cd BlackParticle
   ```

2. **Build the Project**:
   Using Maven, navigate to the project directory and run:
   ```bash
   mvn clean install
   ```

3. **Run the Application**:
   Execute the application using the following command:
   ```bash
   java -jar target/BlackParticle.jar
   ```

## Usage

1. **Opening Files**: Click on the "File" menu and select "Open catalog" to navigate and open files.
2. **Editing Text**: Use the text area to write and edit your code. Syntax highlighting will assist you in identifying code structures.
3. **Running Commands**: Input commands directly into the console area at the bottom of the window and press Enter to execute.
4. **Saving Files**: Use the shortcut **Ctrl + S** to save your current work. If it's a new file, a dialog will prompt you to select the save location.
5. **Changing Theme**: Toggle between dark and light themes using the "Change theme" option in the menu.

## Code Overview

The core functionality of BlackParticle is implemented in the `Main` class, which extends `JFrame`. Key components include:

- **Text Area**: A `RSyntaxTextArea` for editing code with syntax highlighting.
- **Console Area**: A `JTextArea` for displaying output from executed commands.
- **Project Tree**: A `JTree` to navigate through files in the project directory.
- **Menus**: JMenuBar with options for file operations and theme toggling.

### Key Classes

- `RSyntaxTextArea`: Provides the text area with syntax highlighting.
- `JFileChooser`: Used for file selection dialogs.
- `SwingWorker`: Handles command execution in the background.

## Contribution

Feel free to contribute by submitting issues or pull requests. Your feedback and contributions are welcome!

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

This project uses the **RSyntaxTextArea** library for syntax highlighting and **Swing** for building the user interface.

---

Feel free to modify any part of this README to better fit your project!
