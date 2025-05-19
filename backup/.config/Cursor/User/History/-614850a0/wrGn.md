<h1 align="center">
  dotfiles
</h1>
<div align="center">
  <h3>Follow me: </h3>
</div>

<div align="center">
 <p>
    <img src="https://github.com/k1enn/software-engineer-notes/blob/main/subjects/web-programming/Buoi1/Bai01/images/github.png" alt="GitHub Logo" width="20" height="20" />
    <strong><a style="text-decoration:none;" href="https://github.com/k1enn" target="_blank">GitHub</a></strong>
    <img style="padding-left: 10px; " src="https://github.com/k1enn/software-engineer-notes/blob/main/subjects/web-programming/Buoi1/Bai01/images/codeforces.png" alt="Codeforces Logo" width="20" height="20" />
    <strong><a style="text-decoration:none;" href="https://codeforces.com/profile/dinhtrungkien" target="_blank">Codeforces</a></strong>
    <img style="padding-left: 10px;" src="https://github.com/k1enn/software-engineer-notes/blob/main/subjects/web-programming/Buoi1/Bai01/images/linkedin.png" alt="LinkedIn Logo" width="20" height="20" />
    <strong><a style="text-decoration:none;" href="https://www.linkedin.com/in/k1enn/" target="_blank">LinkedIn</a></strong>
  </p>
      <small> November, 2024</small>
</div>

Backup my dotfiles in case Fedora system expoded.

## **System informations**

- **Linux distro**: Fedora Workstation 42.
- **DE**: GNOME 48.
- **DRM**: Wayland.
- **Kernel**: ASUS ROG.

---

## **How to install**

### **Install Prerequisites**

**Ubuntu / Debian**

```
bash
sudo apt update
sudo apt install git stow
```

**Arch Linux**

```
bash
sudo pacman -S git stow
```

**Fedora**

```
bash
sudo dnf install git stow
```

### **Installation**

1. **Clone the repository**

```bash
git clone https://github.com/k1enn/dotfiles.git ~/.dotfiles
cd ~/.dotfiles
```

2. **Run the setup script**

⚠️**WARNING**⚠️
Running this script might **OVERRIDE** your current dotfiles to create symlink. See files structure below to know what you should backup.

```bash
./install.sh
```

This will use GNU Stow to symlink configuration files from each package (e.g. `zsh`, `p10k`, `ideavim`, `git`) into your home directory.

> Alternatively, you can stow individual packages manually:
>
> ```bash
> stow zsh
> stow p10k
> stow ideavim
> stow git
> ```

---

## Customizing Your .gitconfig File

To properly set up your dotfiles with your GitHub account information, follow these instructions to customize your .gitconfig file and set up SSH authentication.

## Setting Up Your Git Identity

1. Open the .gitconfig file in your favorite text editor:

   ```bash
   nano ~/.dotfile/git/.gitconfig
   ```

2. Update the [user] section with your personal information:

   ```
   [user]
       name = Your Name
       email = your_email@example.com
   ```

3. Replace "Your Name" with the name you want to appear on your commits and "your_email@example.com" with the email associated with your GitHub account.

## Generating SSH Keys for GitHub Authentication

Follow these steps to generate SSH keys for secure GitHub access:

1. Check for existing SSH keys first:

   ```bash
   ls -al ~/.ssh
   ```

2. If you don't have existing keys (like id_ed25519.pub or id_rsa.pub), generate a new key:

   ```bash
   ssh-keygen -t ed25519 -C "your_email@example.com"
   ```

   If you're using an older system that doesn't support Ed25519:

   ```bash
   ssh-keygen -t rsa -b 4096 -C "your_email@example.com"
   ```

3. When prompted for a file location, press Enter to accept the default.

4. Enter a secure passphrase when prompted (recommended for security).

## Starting the SSH Agent

1. Start the SSH agent in the background:

   ```bash
   eval "$(ssh-agent -s)"
   ```

2. Add your SSH private key to the agent:
   ```bash
   ssh-add ~/.ssh/id_ed25519
   ```
   (Use `ssh-add ~/.ssh/id_rsa` if you created an RSA key).

## Adding Your SSH Key to GitHub

1. Copy your public SSH key to the clipboard:

   ```bash
   cat ~/.ssh/id_ed25519.pub
   ```

   (Or `cat ~/.ssh/id_rsa.pub` for RSA keys).

2. Log in to your GitHub account in a web browser.

3. Click on your profile photo in the top-right corner, then select **Settings**.

4. In the sidebar, click on **SSH and GPG keys**.

5. Click the **New SSH key** or **Add SSH key** button.

6. In the "Title" field, add a descriptive label for your key (e.g., "Personal Laptop").

7. Paste your copied public key into the "Key" field.

8. Click the **Add SSH key** button to save.

## Testing Your SSH Connection

Verify your configuration is working:

```bash
ssh -T git@github.com
```
