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
        disable_nvimtree_bg = true,
        group_overrides = {
          -- Better visual mode highlight - muted purple bg
          Visual = { bg = "#33395a", fg = "NONE" },
          VisualNOS = { bg = "#33395a", fg = "NONE" },
          -- Line number column matches background
          LineNr = { bg = "NONE" },
          CursorLineNr = { bg = "NONE" },
          SignColumn = { bg = "NONE" },
          -- Vertical separator and status line transparent
          VertSplit = { bg = "NONE", fg = "#393939" },
          WinSeparator = { bg = "NONE", fg = "#393939" },
          StatusLine = { bg = "NONE" },
          StatusLineNC = { bg = "NONE" },
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
      -- Override highlights when oxocarbon colorscheme is loaded
      vim.api.nvim_create_autocmd("ColorScheme", {
        pattern = "oxocarbon",
        callback = function()
          -- Load wallust colors
          local ok, wallust = pcall(require, "wallust.colors")
          local bg_color = ok and wallust.background

          -- Comment color
          vim.api.nvim_set_hl(0, "Comment", { fg = "#6A9955", italic = true })
          vim.api.nvim_set_hl(0, "@comment", { fg = "#6A9955", italic = true })

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
          vim.api.nvim_set_hl(0, "StatusLineNC", { bg = bg_color })

          -- Telescope file path visibility (brighter for dark backgrounds)
          local path_color = "#8a8a8a"
          vim.api.nvim_set_hl(0, "TelescopeResultsComment", { fg = path_color })
          vim.api.nvim_set_hl(0, "TelescopeResultsSpecialComment", { fg = path_color })
          vim.api.nvim_set_hl(0, "TelescopeResultsClass", { fg = path_color })
          vim.api.nvim_set_hl(0, "TelescopeResultsIdentifier", { fg = path_color })
          vim.api.nvim_set_hl(0, "TelescopeResultsStruct", { fg = path_color })
          vim.api.nvim_set_hl(0, "TelescopeResultsDiffUntracked", { fg = path_color })
          vim.api.nvim_set_hl(0, "TelescopeResultsVariable", { fg = path_color })
          vim.api.nvim_set_hl(0, "TelescopeResultsConstant", { fg = path_color })
          vim.api.nvim_set_hl(0, "TelescopeResultsNumber", { fg = path_color })
          vim.api.nvim_set_hl(0, "TelescopeResultsField", { fg = path_color })
          vim.api.nvim_set_hl(0, "NonText", { fg = path_color })
          vim.api.nvim_set_hl(0, "Conceal", { fg = path_color })
          vim.api.nvim_set_hl(0, "Directory", { fg = "#4EC9B0" })
          vim.api.nvim_set_hl(0, "Comment", { fg = "#6A9955", italic = true })
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
}
