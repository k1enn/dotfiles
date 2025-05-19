import 'package:flutter/material.dart';
import 'recipe.dart';

class RecipeDetail extends StatefulWidget {
  final Recipe recipe;

  const RecipeDetail({
    super.key,
    required this.recipe,
  });

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
            // TO DO: Add Expanded
            Expanded(
              child: ListView.builder(
                padding: const EdgeInsets.all(7.0),
                itemCount: widget.recipe.ingredient.length,
                itemBuilder: (BuildContext context, int index) {
                  return Text('${ingredient.quantity} ${ingredient.measure} ${ingredient.name}');
                },
            ),
            ),
            // TO DO: Add Slider() 
          ],
        )
      )
    );
  }
}