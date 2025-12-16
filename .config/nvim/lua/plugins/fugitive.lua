return {
  {
    "tpope/vim-fugitive",
    cmd = { "Git", "Gdiffsplit", "Gvdiffsplit", "Gread", "Gwrite", "GBrowse", "Gblame" },
    keys = {
      { "<leader>gs", "<cmd>Git<cr>", desc = "Git status (Fugitive)" },
      { "<leader>gc", "<cmd>Git commit<cr>", desc = "Git commit (Fugitive)" },
      { "<leader>gl", "<cmd>Git log<cr>", desc = "Git log (Fugitive)" },
      { "<leader>gb", "<cmd>Git blame<cr>", desc = "Git blame (Fugitive)" },
      { "<leader>gD", "<cmd>Gdiffsplit<cr>", desc = "Diff (index ↔ worktree)" },
    },
  },
}
