import mongoose from "mongoose";

// Put new at the beginning of the name to make it a model
const productSchema = new mongoose.Schema({
  name: { type: String, required: true },
  price: { type: Number, required: true }, // Javascript object
});
