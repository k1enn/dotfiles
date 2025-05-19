import React from "react";
import { Card } from "react-bootstrap";
import products from "../products";
const Product = () => {
  return (
    <Card className="my-3 py-3 rounded">
      <a href={`/product/${products._id}`}>
        <Card.Img src={products.image}></Card.Img>
      </a>
    </Card>
  );
};

export default Product;
