return {
  {
    "Mofiqul/vscode.nvim",
    lazy = false,
    priority = 1000,
    config = function()
      local vscode = require("vscode")
      vscode.setup({
        transparent = false,
        italic_comments = true,
        underline_links = true,
        disable_nvimtree_bg = true,
        group_overrides = {
          -- Better TS/JS highlighting
          ["@variable"] = { fg = "#9CDCFE" },
          ["@variable.builtin"] = { fg = "#9CDCFE" },
          ["@property"] = { fg = "#9CDCFE" },
          ["@function"] = { fg = "#DCDCAA" },
          ["@function.call"] = { fg = "#DCDCAA" },
          ["@method"] = { fg = "#DCDCAA" },
          ["@method.call"] = { fg = "#DCDCAA" },
          ["@keyword"] = { fg = "#569CD6" },
          ["@keyword.function"] = { fg = "#569CD6" },
          ["@keyword.return"] = { fg = "#C586C0" },
          ["@type"] = { fg = "#4EC9B0" },
          ["@type.builtin"] = { fg = "#4EC9B0" },
          ["@constructor"] = { fg = "#4EC9B0" },
          ["@string"] = { fg = "#CE9178" },
          ["@number"] = { fg = "#B5CEA8" },
          ["@boolean"] = { fg = "#4e94ce" },
          ["@operator"] = { fg = "#D4D4D4" },
          ["@punctuation"] = { fg = "#D4D4D4" },
          ["@comment"] = { fg = "#6A9955", italic = true },
          -- SQL specific
          ["@keyword.sql"] = { fg = "#569CD6" },
          ["@type.sql"] = { fg = "#4EC9B0" },
        },
      })
    end,
  },
  {
    "LazyVim/LazyVim",
    opts = {
      colorscheme = "vscode",
    },
  },
}
