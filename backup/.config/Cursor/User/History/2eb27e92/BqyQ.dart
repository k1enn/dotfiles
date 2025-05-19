import 'package:flutter/material.dart';
import 'recipe.dart';

class RecipeDetail extends StatefulWidget {
  final Recipe recipe;

  const RecipeDetail({
    Key? key,
    required this.recipe,
  }) : super(key: key);

  @override
  State<StatefulWidget> createState() {
    return _RecipeDetailsState();  
  }
}

class _RecipeDetailsState extends State<RecipeDetail> {
  // Add _sliderVal

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.recipe.label),
      ),
      body: SafeArea(
        child: Column(
          children: <Widget>[
            SizedBox( 
              height: 300,
              width: double.infinity,
              child: Image(image: AssetImage(widget.recipe.imageUrl),), // Adding image from recipe class
            ),
            const SizedBox(height: 4,),
            Text(
              widget.recipe.label,
              style: const TextStyle(fontSize: 18),
            ),
          ],
        )
      )
    )
  }
}