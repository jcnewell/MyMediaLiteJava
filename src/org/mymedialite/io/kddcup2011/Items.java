// Copyright (C) 2011 Zeno Gantner, CHris Newell
//
// This file is part of MyMediaLite.
//
// MyMediaLite is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// MyMediaLite is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with MyMediaLite.  If not, see <http://www.gnu.org/licenses/>.

package org.mymedialite.io.kddcup2011;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.mymedialite.data.KDDCupItems;
import org.mymedialite.taxonomy.KDDCupItemType;

/**
 * Routines for reading in the item taxonomy of the KDD Cup 2011 data.
 * @version 2.03
 */
public class Items {

  /**
   * Read in the item data from several files.
   * @param tracks_filename name of the tracks file
   * @param albums_filename name of the album/record file
   * @param artists_filename name of the artists file
   * @param genres_filename name of the genre file
   * @param track_no 1 or 2
   * @return the rating data
   * @throws IOException 
   */
  public static KDDCupItems read(String tracks_filename,
                                 String albums_filename,
                                 String artists_filename,
                                 String genres_filename,
                                 int track_no) throws IOException {

    KDDCupItems items = new KDDCupItems(track_no == 1 ? 624961 : 296111);
    readTracks(new  BufferedReader(new FileReader(tracks_filename)),  items);
    readAlbums(new  BufferedReader(new FileReader(albums_filename)),  items);
    readArtists(new BufferedReader(new FileReader(artists_filename)), items);
    readGenres(new  BufferedReader(new FileReader(genres_filename)),  items);
    return items;
  }

  /**
   * Read the track data.
   * @param reader a reader object to read the data from
   * @param items the <see cref="KDDCupItems"/> object
   * @throws IOException 
   */
  public static void readTracks(BufferedReader reader, KDDCupItems items) throws IOException {
    String line;
    while ((line = reader.readLine()) != null) {
      String[] tokens = line.split("|");
      int track_id  = Integer.parseInt(tokens[0]);
      int album_id  = tokens[1] == "None" ? -1 : Integer.parseInt(tokens[1]);
      int artist_id = tokens[2] == "None" ? -1 : Integer.parseInt(tokens[2]);

      int[] genres = new int[tokens.length - 3];
      for (int i = 0; i < genres.length; i++)
        genres[i] = Integer.parseInt(tokens[3 + i]);

      items.insert(track_id, KDDCupItemType.TRACK, album_id, artist_id, genres);
    }
  }

  /**
   * Read the album data.
   * @param reader a reader object to read the data from
   * @param items the <see cref="KDDCupItems"/> object
   * @throws IOException 
   */
  public static void readAlbums(BufferedReader reader, KDDCupItems items) throws IOException {
    String line;
    while ((line = reader.readLine()) != null) {
      String[] tokens = line.split("|");
      int album_id  = Integer.parseInt(tokens[0]);
      int artist_id = tokens[1] == "None" ? -1 : Integer.parseInt(tokens[1]);
      int[] genres = new int[tokens.length - 2];
      for (int i = 0; i < genres.length; i++)
        genres[i] = Integer.parseInt(tokens[2 + i]);

      items.insert(album_id, KDDCupItemType.ALBUM, album_id, artist_id, genres);
    }
  }

  /**
   * Read the artist data.
   * @param reader a reader object to read the data from
   * @param items the <see cref="KDDCupItems"/> object
   * @throws IOException 
   */
  public static void readArtists(BufferedReader reader, KDDCupItems items) throws IOException {
    String line;
    while ((line = reader.readLine()) != null) {
      int artist_id = Integer.parseInt(line);
      items.insert(artist_id, KDDCupItemType.ARTIST, -1, artist_id, null);
    }
  }

  /**
   * Read the genre data.
   * @param reader a reader object to read the data from
   * @param items the <see cref="KDDCupItems"/> object
   * @throws IOException
   */
  public static void readGenres(BufferedReader reader, KDDCupItems items) throws IOException {
    String line;
    while ((line = reader.readLine()) != null) {
      int genre_id = Integer.parseInt(line);
      items.insert(genre_id, KDDCupItemType.GENRE, -1, -1, null);
    }
  }

}
