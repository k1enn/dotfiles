return {
  "nvim-telescope/telescope.nvim",
  opts = {
    defaults = {
      file_ignore_patterns = {}, -- don't ignore any patterns
    },
    pickers = {
      find_files = {
        hidden = true, -- show hidden files
      },
      live_grep = {
        additional_args = { "--hidden" }, -- search in hidden files
      },
    },
  },
}
