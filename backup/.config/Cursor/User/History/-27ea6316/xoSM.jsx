import { Button, Container } from "@chakra-ui/react";
import React from "react";

const Navbar = () => {
  return (
    <Container maxW={"1140px"} px={4}>
      <Flex
        h={16}
        justifyContent={"space-between"}
        alignItems={"center"}
        flexDir={{
          base: "column",
          sm: "row",
        }}
      >
        <Text
          fontSize={{ base: "22", sm: "28" }}
          fontWeight={"bold"}
          textTransform={"uppercase"}
          textAlign={"center"}
          bgGradient={"linear(to-r, cyan.400, blue.500)"}
          bgClip={"text"}
        >
          <Link to="/">Product Store</Link>
        </Text>

        <HStack spacing={2} alignItems={"center"}>
          <Link to="/">
          <Button>
            <PlusSquareIcon />
            Product
          </Button>
          </Link>
          <Link to="/create">Create</Link>
        </HStack>
      </Flex>
    </Container>
  );
};

export default Navbar;
