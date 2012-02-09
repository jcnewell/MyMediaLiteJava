// Copyright (C) 2011 Zeno Gantner, Chris Newell
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

package org.mymedialite.data;

import java.util.ArrayList;
import java.util.List;
import org.mymedialite.taxonomy.KDDCupItemType;

/**
 * Represents KDD Cup 2011 items like album, track, artist, or genre.
 * @version 2.03
 */
public final class KDDCupItems {
  
  List<int[]> genres;
  List<Integer> artists;
  List<Integer> albums;
  List<KDDCupItemType> types;

  static int[] empty_list = new int[0];

  /**
   * Create item information object.
   * @param size the number of items
   */
  public KDDCupItems(int size) {
    genres  = new ArrayList<int[]>(size);
    artists = new ArrayList<Integer>(size);
    albums  = new ArrayList<Integer>(size);
    types   = new ArrayList<KDDCupItemType>(size);

    for (int i = 0; i < size; i++) {
      artists.set(i, -1);
      albums.set(i, -1);
      types.set(i, KDDCupItemType.NONE);
    }
  }

  /**
   * Insert information about an entry to the data structure.
   * @param item_id the item ID
   * @param type the KDDCupItemType of the item
   * @param album the album ID if the item is a track or album, -1 otherwise
   * @param artist the artist ID if the item is a track, an album, or an artist, -1 otherwise
   * @param genres a (possibly empty or null) list of genre IDs
   */
  public void insert(int item_id, KDDCupItemType type, int album, int artist, int[] genres) {
    this.types.set(item_id, type);
    this.albums.set(item_id, album);
    this.artists.set(item_id, artist);
    this.genres.set(item_id, genres);
  }

  /**
   * Get the type of a given item.
   * @param item_id the item ID
   * @return the KDDCupItemType of the given item
   */
  public KDDCupItemType getType(int item_id) {
    return types.get(item_id);
  }

  /**
   * Get a list of genres for a given item.
   * @param item_id the item ID
   * @return a list of genres
   */
  public int[] getGenres(int item_id) {
    return genres.get(item_id) != null ? genres.get(item_id) : empty_list;
  }

  /**
   * Get the artist for a given item.
   * @param item_id the item ID
   * @return the artist ID
   */
  public int getArtist(int item_id) {
    return artists.get(item_id);
  }

  /**
   * Get the album for a given item.
   * @param item_id the item ID
   * @return the album ID
   */
  public int getAlbum(int item_id) {
    return albums.get(item_id);
  }

  /**
   * Check whether the given item is associated with an album.
   * @param item_id the item ID
   * @return true if it is associated with an album, false otherwise
   */
  public boolean hasAlbum(int item_id) {
    return albums.get(item_id) != -1;
  }

  /**
   * Check whether the given item is associated with an artist.
   * @param item_id the item ID
   * @return true if it is associated with an artist, false otherwise
   */
  public boolean hasArtist(int item_id) {
    return artists.get(item_id) != -1;
  }

  /**
   * Check whether the given item is associated with one or more genres.
   * @param item_id the item ID
   * @return true if it is associated with at least one genre, false otherwise
   */
  public boolean hasGenres(int item_id) {
    if (genres.get(item_id) == null)
      return false;
    return genres.get(item_id).length > 0;
  }

  /**
   * Gives a textual summary of the item data.
   */
  public String toString() {
    int num_tracks = 0, num_albums = 0, num_artists = 0, num_genres = 0;
    for (KDDCupItemType type : types)
      if(type.equals(KDDCupItemType.TRACK))       num_tracks++;
      else if(type.equals(KDDCupItemType.ALBUM))  num_albums++;
      else if(type.equals(KDDCupItemType.ARTIST)) num_artists++;
      else if(type.equals(KDDCupItemType.GENRE))  num_genres++;

    return num_tracks + " tracks, " + num_albums + " albums, " + num_artists + " artists, " + num_genres + " genres";
  }

}

