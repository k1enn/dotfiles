return {
  {
    "nvim-neotest/neotest",
    dependencies = {
      "nvim-neotest/nvim-nio",
      "nvim-lua/plenary.nvim",
      "antoinemadec/FixCursorHold.nvim",
      "nvim-treesitter/nvim-treesitter",
      -- VSTest adapter (better maintained than neotest-dotnet)
      "Nsidorenco/neotest-vstest",
    },
    keys = {
      { "<leader>Tt", function() require("neotest").run.run() end, desc = "Run Nearest Test" },
      { "<leader>Tf", function() require("neotest").run.run(vim.fn.expand("%")) end, desc = "Run File Tests" },
      { "<leader>Ta", function() require("neotest").run.run(vim.fn.getcwd()) end, desc = "Run All Tests" },
      { "<leader>Ts", function() require("neotest").summary.toggle() end, desc = "Toggle Summary" },
      { "<leader>To", function() require("neotest").output.open({ enter = true, auto_close = true }) end, desc = "Show Output" },
      { "<leader>TO", function() require("neotest").output_panel.toggle() end, desc = "Toggle Output Panel" },
      { "<leader>Tx", function() require("neotest").run.stop() end, desc = "Stop Test" },
      { "<leader>Td", function() require("neotest").run.run({ strategy = "dap" }) end, desc = "Debug Nearest Test" },
      { "<leader>Tl", function() require("neotest").run.run_last() end, desc = "Run Last Test" },
      { "<leader>TL", function() require("neotest").run.run_last({ strategy = "dap" }) end, desc = "Debug Last Test" },
      { "<leader>Tw", function() require("neotest").watch.toggle(vim.fn.expand("%")) end, desc = "Toggle Watch" },
      { "[T", function() require("neotest").jump.prev({ status = "failed" }) end, desc = "Prev Failed Test" },
      { "]T", function() require("neotest").jump.next({ status = "failed" }) end, desc = "Next Failed Test" },
    },
    opts = {
      status = { virtual_text = true },
      output = { open_on_run = true },
      quickfix = {
        open = function()
          vim.cmd("copen")
        end,
      },
    },
    config = function(_, opts)
      opts.adapters = {
        require("neotest-vstest")({
          dap_settings = {
            type = "coreclr",
          },
        }),
      }
      require("neotest").setup(opts)
    end,
  },
}
