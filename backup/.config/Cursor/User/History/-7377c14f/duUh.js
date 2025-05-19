import express from "express";
import dotenv from "dotenv";
import mongoose from "mongoose";
import { connectDB } from "./config/db.js";
import Product from "./models/product.model.js";

dotenv.config();
const app = express();
app.use(express.json()); // Allow accepting JSON data in the request body


app.listen(5000, () => {
  connectDB();
  console.log("Server started at http://localhost:5000");
});
