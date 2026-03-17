return {
  "nvim-neo-tree/neo-tree.nvim",
  opts = {
    default_component_configs = {
      indent = {
        with_expanders = true,
      },
    },
    window = {
      mappings = {},
    },
    event_handlers = {
      {
        event = "neo_tree_window_after_open",
        handler = function()
          vim.cmd("setlocal winhighlight=Normal:Normal,NormalNC:NormalNC,EndOfBuffer:EndOfBuffer,CursorLine:CursorLine,SignColumn:SignColumn")
        end,
      },
    },
  },
}
