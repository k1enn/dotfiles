import React from "react";
import { Card } from "react-bootstrap";
import products from "../products";

const Product = ({ product }) => {
  return (
    <Card className="my-3 py-3 rounded">
      <a href={`/product/${product._id}`}>
        <Card.Img src={product.image} variant="top"></Card.Img>
      </a>
      <Card.Body>
        <a href={`/product/${product._id}`}>
          <Card.Title as="div">
            
          </Card.Title>
        </a>
    </Card>
  );
};

export default Product;
