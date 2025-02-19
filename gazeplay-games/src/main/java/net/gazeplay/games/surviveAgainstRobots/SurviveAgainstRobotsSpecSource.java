package net.gazeplay.games.surviveAgainstRobots;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class SurviveAgainstRobotsSpecSource implements GameSpecSource {

    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("SurviveAgainstRobots").gameThumbnail("data/Thumbnails/surviveAgainstRobots.png")
                .category(GameCategories.Category.ACTION_REACTION).build(),
            new SurviveAgainstRobotsVariantGenerator(), new SurviveAgainstRobotsLauncher());
    }

}
