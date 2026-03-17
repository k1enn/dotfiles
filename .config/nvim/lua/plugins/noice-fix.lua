-- Fix noice.nvim compatibility with Roslyn LSP progress
return {
  "folke/noice.nvim",
  opts = {
    routes = {
      -- Filter out Roslyn progress messages that cause errors
      {
        filter = {
          event = "lsp",
          kind = "progress",
          cond = function(message)
            local client = vim.tbl_get(message.opts, "progress", "client")
            return client == "roslyn"
          end,
        },
        opts = { skip = true },
      },
    },
  },
}
