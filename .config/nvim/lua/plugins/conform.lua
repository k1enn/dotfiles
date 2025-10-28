return {
  {
    "stevearc/conform.nvim",
    opts = {
      formatters_by_ft = {
        cs = { "csharpier" },
        razor = { "html-lsp" }, -- Use HTML formatter for Razor files
        ["html.cshtml.razor"] = { "html-lsp" },
      },
    },
  },
}
