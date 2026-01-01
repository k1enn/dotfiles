return {
  {
    "Mofiqul/vscode.nvim",
    lazy = false,
    priority = 1000,
    config = function()
      local vscode = require("vscode")
      vscode.setup({
        transparent = true,
        italic_comments = true,
        underline_links = true,
        disable_nvimtree_bg = false,
        group_overrides = {
          -- Transparent neo-tree
          NeoTreeNormal = { bg = "NONE" },
          NeoTreeNormalNC = { bg = "NONE" },
          NeoTreeEndOfBuffer = { bg = "NONE" },
          NeoTreeWinSeparator = { bg = "NONE" },
          NeoTreeFloatBorder = { bg = "NONE" },
          NeoTreeFloatTitle = { bg = "NONE" },
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
    "nyoom-engineering/oxocarbon.nvim",
    lazy = false,
    priority = 1000,
    config = function()
      -- Override Visual mode highlights for better visibility
      vim.api.nvim_create_autocmd("ColorScheme", {
        pattern = "oxocarbon",
        callback = function()
          -- Better visual mode highlight - muted purple bg
          vim.api.nvim_set_hl(0, "Visual", { bg = "#33395a", fg = "NONE" })
          vim.api.nvim_set_hl(0, "VisualNOS", { bg = "#33395a", fg = "NONE" })

          -- Line number column matches background
          vim.api.nvim_set_hl(0, "LineNr", { bg = "NONE" })
          vim.api.nvim_set_hl(0, "CursorLineNr", { bg = "NONE" })
          vim.api.nvim_set_hl(0, "SignColumn", { bg = "NONE" })

          -- Vertical separator and status line transparent
          vim.api.nvim_set_hl(0, "VertSplit", { bg = "NONE", fg = "#393939" })
          vim.api.nvim_set_hl(0, "WinSeparator", { bg = "NONE", fg = "#393939" })
          vim.api.nvim_set_hl(0, "StatusLine", { bg = "NONE" })
          vim.api.nvim_set_hl(0, "StatusLineNC", { bg = "NONE" })
        end,
      })
    end,
  },
  {
    "LazyVim/LazyVim",
    opts = {
      colorscheme = "oxocarbon",
    },
  },
  {
    "nvim-lualine/lualine.nvim",
    opts = {
      options = {
        theme = {
          normal = {
            a = { bg = "NONE" },
            b = { bg = "NONE" },
            c = { bg = "NONE" },
          },
          insert = {
            a = { bg = "NONE" },
            b = { bg = "NONE" },
            c = { bg = "NONE" },
          },
          visual = {
            a = { bg = "NONE" },
            b = { bg = "NONE" },
            c = { bg = "NONE" },
          },
          replace = {
            a = { bg = "NONE" },
            b = { bg = "NONE" },
            c = { bg = "NONE" },
          },
          command = {
            a = { bg = "NONE" },
            b = { bg = "NONE" },
            c = { bg = "NONE" },
          },
          inactive = {
            a = { bg = "NONE" },
            b = { bg = "NONE" },
            c = { bg = "NONE" },
          },
        },
      },
    },
  },
}
