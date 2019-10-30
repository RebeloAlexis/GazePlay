package net.gazeplay.games.mediaPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.games.GazePlayDirectories;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MediaFileReader {

    Configuration c;

    List<MediaFile> mediaList;

    int index;

    @Getter
    @Setter
    int playing;

    public MediaFileReader() {
        mediaList = new ArrayList<>();
        index = -1;
        playing = -1;
        try {
            c = Configuration.getInstance();

            File f0 = new File(GazePlayDirectories.getGazePlayFolder() + "profiles" + GazePlayDirectories.FILESEPARATOR + c.getUserName()
                    + GazePlayDirectories.FILESEPARATOR + "/data/mediaPlayer");
            f0.mkdirs();
            File f = new File(f0, "playerList.csv");
            f.createNewFile();

            BufferedReader b = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));

            String readLine;

            while ((readLine = b.readLine()) != null) {
                String[] split = readLine.split(",");
                if (split.length == 3 || split[3] == null || split[3].equals("")) {
                    mediaList.add(new MediaFile(split[0], split[1], split[2], null));
                } else {
                    mediaList.add(new MediaFile(split[0], split[1], split[2], split[3]));
                }
            }

            b.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MediaFile next() {
        if (mediaList.size() > 0) {
            MediaFile mf = mediaList.get(index = (index + 1) % mediaList.size());
            return mf;
        }
        return null;
    }

    public MediaFile previous() {
        if (mediaList.size() > 0) {
            MediaFile mf = mediaList.get((index - 3 + 3 * mediaList.size()) % mediaList.size());
            index = (index - 1 + mediaList.size()) % mediaList.size();
            return mf;
        }
        return null;
    }

    public MediaFile nextPlayed() {
        if (mediaList.size() > 0) {
            playing = (playing + 1) % mediaList.size();
            return mediaList.get(playing);
        }
        return null;
    }

    public MediaFile prevPlayed() {
        if (mediaList.size() > 0) {
            playing = (playing - 1 + mediaList.size()) % mediaList.size();
            return mediaList.get(playing);
        }
        return null;
    }

    public void addMedia(MediaFile mf) {
        try {
            c = Configuration.getInstance();

            File f;

            if (c.getUserName() == null || c.getUserName().equals("")) {
                f = new File(GazePlayDirectories.getGazePlayFolder() + "data/mediaPlayer/playerList.csv");
            } else {
                f = new File(GazePlayDirectories.getGazePlayFolder() + "profiles" + GazePlayDirectories.FILESEPARATOR + c.getUserName()
                        + GazePlayDirectories.FILESEPARATOR + "/data/mediaPlayer/playerList.csv");
            }

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f, true), StandardCharsets.UTF_8));

            if (mediaList.size() == 0) {
                bw.write("" + mf.getType() + "," + mf.getPath() + "," + mf.getName() + "," + mf.getImagepath());
            } else {
                bw.write("\n" + mf.getType() + "," + mf.getPath() + "," + mf.getName() + "," + mf.getImagepath());
            }

            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaList.add(mf);

    }
}
