import { StrictMode } from 'react'
import { ChakraProvider } from "@chakra-ui/react"
import { createRoot } from 'react-dom/client'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <ChakraProvider>
      <App />
    </ChakraProvider>
  </StrictMode>,
)
