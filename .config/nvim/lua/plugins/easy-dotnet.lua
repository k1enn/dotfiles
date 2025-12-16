return {
  {
    "GustavEikaas/easy-dotnet.nvim",
    dependencies = {
      "nvim-lua/plenary.nvim",
      "nvim-telescope/telescope.nvim",
    },
    config = function()
      require("easy-dotnet").setup()
    end,
  },
  {
    "hrsh7th/nvim-cmp",
    dependencies = { "GustavEikaas/easy-dotnet.nvim" },
    opts = function(_, opts)
      local cmp = require("cmp")
      cmp.register_source("easy-dotnet", require("easy-dotnet").package_completion_source)

      table.insert(opts.sources, { name = "easy-dotnet" })
    end,
  },
}
