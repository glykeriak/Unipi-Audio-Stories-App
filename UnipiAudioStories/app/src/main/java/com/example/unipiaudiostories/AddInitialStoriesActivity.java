package com.example.unipiaudiostories;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddInitialStoriesActivity extends AppCompatActivity {

    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase.getInstance().getReference();
        resetStoriesInDatabase();
    }

    /**
     * Διαγράφει τις υπάρχουσες ιστορίες από τη βάση δεδομένων
     * και προσθέτει τις αρχικές ιστορίες.
     */
    private void resetStoriesInDatabase() {
        database.child("stories").removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                addInitialStories();
            } else {
                Toast.makeText(this, "Αποτυχία διαγραφής προηγούμενων ιστοριών!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void addInitialStories() {
        Story story1 = new Story("Little Red Riding Hood", "Once upon a time, there was a little girl everyone called Little Red Riding Hood because of the beautiful red cape her grandmother had sewn for her. One day, her mother gave her a basket of food to take to her sick grandmother. Before she left, her mother said, \"Be careful and don’t talk to strangers.\"\n" +
                "\n" +
                "On her way, Little Red Riding Hood met a wolf. He asked her where she was going, and without thinking, she told him everything. The wolf, who wanted to eat both her and her grandmother, ran ahead to her grandmother’s house. He ate the grandmother, dressed in her clothes, and lay in her bed.\n" +
                "\n" +
                "When Little Red Riding Hood arrived, she found the \"wolf-grandmother\" and noticed she looked different. \"Grandmother, what big ears you have!\" she said. \"The better to hear you with,\" replied the wolf. \"Grandmother, what big eyes you have!\" she continued. \"The better to see you with,\" he said. \"Grandmother, what big teeth you have!\" exclaimed Little Red Riding Hood. \"The better to eat you with!\" said the wolf and lunged to grab her.\n" +
                "\n" +
                "Fortunately, a hunter passing by heard the screams and came into the house. He saved Little Red Riding Hood and her grandmother, and the wolf ran away. From that day on, Little Red Riding Hood learned to be careful and never talk to strangers.", "https://drive.google.com/uc?id=1pq3SIzw5-cNQHwwoZMUujKnF-FgebkB9");

        Story story2 = new Story("Cinderella", "Once upon a time, there was a kind-hearted girl named Cinderella. Her stepmother and two stepsisters made her do all the household chores. They treated her harshly and mocked her for her poor clothes and appearance.\n" +
                "\n" +
                "One day, a messenger announced that the king was hosting a ball to find a bride for the prince. Cinderella’s stepsisters prepared with excitement, but they wouldn’t let her go. Left alone, Cinderella began to cry. Then, her fairy godmother appeared. With her magic wand, the fairy godmother transformed her rags into a beautiful gown and gave her a pair of glass slippers. She also turned a pumpkin into a carriage and the mice into horses.\n" +
                "\n" +
                "\"Remember,\" she said, \"you must return before midnight, for everything will return to its original state.\" At the ball, Cinderella caught the prince’s attention. They danced together all evening, but when the clock struck midnight, Cinderella ran away, leaving behind one glass slipper.\n" +
                "\n" +
                "The prince used the slipper to find her. When he reached her house, the stepsisters tried to wear it, but it didn’t fit. When it was Cinderella’s turn, the slipper fit perfectly. The prince took her to the palace, and they lived happily ever after.", "https://drive.google.com/uc?id=1ctX7SUciF8RG7CnmhBhCdIZ-rOvmamd3");

        Story story3 = new Story("Snow White and the Seven Dwarfs", "Once upon a time, there was a beautiful princess named Snow White. She had a kind heart, but her stepmother, the Evil Queen, was jealous of her beauty. Every day, the Queen asked her magic mirror, \"Mirror, mirror on the wall, who is the fairest of them all?\" When the mirror answered \"Snow White,\" the Queen grew angry and plotted to get rid of her.\n" +
                "\n" +
                "Snow White fled into the forest and found a cottage belonging to seven dwarfs. She lived happily with them, but the Queen discovered her hiding place. Disguised as an old woman, she gave Snow White a poisoned apple. One bite, and Snow White fell into a deep sleep.\n" +
                "\n" +
                "The dwarfs placed her in a glass coffin, heartbroken. One day, a prince came by and kissed her. The spell was broken, and Snow White woke up. She married the prince, and they lived happily ever after.", "https://drive.google.com/uc?id=1YX9WIdq44CKHZNEL3va5yoGmEAW3Q5--");

        Story story4 = new Story("The Wolf and the Seven Little Goats", "Once, a mother goat left her seven kids at home and warned them, \"Do not open the door to anyone, especially the wolf.\" The wolf overheard and tried to trick the kids. He made his voice sweet, but they realized it was the wolf. Then, the wolf covered his paws in flour to make them look white like their mother’s.\n" +
                "\n" +
                "He returned, and this time the kids believed him. He entered the house and ate six of them. The seventh hid in the clock. When the mother goat returned, she found her youngest and together they cut open the wolf’s belly, saving the other kids. From then on, everyone learned to be cautious of strangers.", "https://drive.google.com/uc?id=1WCr6Eyy3g4Yzn5eNYmciSnCsa30GkfEt");

        Story story5 = new Story("Goldilocks and the Three Bears", "Once upon a time, a little girl named Goldilocks wandered into the forest and found a house. She knocked on the door, but no one answered, so she went inside. She saw three bowls of porridge and tried them. The first was too hot, the second was too cold, but the third was just right, so she ate it all.\n" +
                "\n" +
                "Then she saw three chairs. The first was too big, the second was too soft, but the third was just right—until it broke! Feeling tired, she went upstairs and found three beds. The first was too hard, the second was too soft, but the third was just right, and she fell asleep.\n" +
                "\n" +
                "Soon, the bears who lived there came home. They saw someone had eaten their porridge, sat in their chairs, and slept in their beds. When they found Goldilocks, she woke up, saw the bears, and ran away, learning never to enter someone’s house without permission.", "https://drive.google.com/uc?id=19WaOn2Y1VpWrTWoJQ8HMzHV3I0DFjhR9");

        Story[] stories = {story1, story2, story3, story4, story5};

        // Προσθήκη των ιστοριών στη βάση δεδομένων
        for (Story story : stories) {
            DatabaseReference storyRef = database.child("stories").push();
            storyRef.setValue(story);

            storyRef.child("totalListens").setValue(0);
        }

        Toast.makeText(this, "Η βάση δεδομένων αρχικοποιήθηκε!", Toast.LENGTH_SHORT).show();

    }

    // Κλάση για την αναπαράσταση μιας ιστορίας.
    public static class Story {
        private String name;
        private String text;
        private String imageUrl;

        public Story() {
        }

        public Story(String name, String text, String imageUrl) {
            this.name = name;
            this.text = text;
            this.imageUrl = imageUrl;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }

}
