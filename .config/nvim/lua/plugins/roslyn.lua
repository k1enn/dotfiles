return {
  "seblyng/roslyn.nvim",
  ft = { "cs" }, -- Only load for C# files
  dependencies = {
    "nvim-lua/plenary.nvim",
  },
  ---@module 'roslyn.config'
  ---@type RoslynNvimConfig
  opts = {
    exe = {
      "dotnet",
      vim.fs.joinpath(vim.fn.stdpath("data"), "roslyn", "Microsoft.CodeAnalysis.LanguageServer.dll"),
    },
  },
}
