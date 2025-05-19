import React from "react";
import { Card } from "react-bootstrap";
import products from "../products";

const Product = ({ product }) => {
  return (
    <Card className="my-3 py-3 rounded">
      <a href={`/product/${products._id}`}>
        <Card.Img src={products.image} variant="top"></Card.Img>
      </a>
    </Card>
  );
};

export default Product;
