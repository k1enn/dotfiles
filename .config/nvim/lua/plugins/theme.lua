return {
  {
    "navarasu/onedark.nvim",
    lazy = false,
    priority = 1000,
    config = function()
      require("onedark").setup({
        style = "darker",
        transparent = true,
      })
      require("onedark").load()
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
      colorscheme = "onedark",
    },
  },
}
