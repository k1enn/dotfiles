import { Container, VStack } from '@chakra-ui/react';
import React from 'react'

const CreatePage = () => {
  const [newProduct, setNewProduct] = useState({
    name: "",
    price: "",
    image: "",
  });

  return (
    <Container maxW={"container.small"}>
      <VStack spacing={8}>
        <Heading>Create New Product</Heading>
        </VStack></Container>
  )
}

export default CreatePage