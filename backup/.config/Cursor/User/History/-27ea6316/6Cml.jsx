import { Container } from '@chakra-ui/react'
import React from 'react'

const Navbar = () => {
  return (
    <Container maxW={"1140px"} px={4}>
      <Flex
       h={16}
       justifyContent={"space-between"}
       alignItems={"center"}
       flexDir={{
        base:"column",
        sm:"row",
      }}>

      </Flex>

    </Container>
  )
}

export default Navbar