package com.example.squash.technology

import com.example.squash.R

class Constants {
    companion object {
        //pagination size
        const val PAGE_SIZE = 8

        //make new post activity
        const val CREATE_POST_ACTIVITY = 2

        //single post activity
        const val VIEW_POST_ACTIVITY = 1

        //image pick code
        const val IMAGE_PICK_CODE = 1000;
        //Permission code
        const val PERMISSION_CODE = 1001;


        val VEGGIES = listOf<Int>(
            R.drawable.ic_apple,
            R.drawable.ic_beetroot,
            R.drawable.ic_bell_pepper,
            R.drawable.ic_broccoli,
            R.drawable.ic_carrot,
            R.drawable.ic_cherry,
            R.drawable.ic_chili,
            R.drawable.ic_corn,
            R.drawable.ic_cucumber,
            R.drawable.ic_eggplant,
            R.drawable.ic_grape,
            R.drawable.ic_orange,
            R.drawable.ic_pineapple,
            R.drawable.ic_strawberry,
            R.drawable.ic_watermelon,
            R.drawable.ic_avocado
        )

        val COLORS = listOf<Int>(
            R.color.red,
            R.color.orange,
            R.color.yellow,
            R.color.green,
            R.color.lime,
            R.color.maroon,
            R.color.blue,
            R.color.teal,
            R.color.turquoise,
            R.color.navy,
            R.color.pink,
            R.color.brown,
            R.color.beige,
            R.color.purple,
            R.color.grey,
            R.color.golden
        )

    }
}
