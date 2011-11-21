package org.mymedialite.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.mymedialite.util.Random;

//Copyright (C) 2011 Zeno Gantner
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
//

/**
 * Abstract dataset class that implements some common functions.
 * @version 2.02
 */
public abstract class DataSet implements IDataSet
{
	/**
	 */
	public List<Integer> getUsers() {
		return users;
	}
	protected List<Integer> users = new Vector<Integer>();
	
	/**
	 */
	public List<Integer> getItems() {
		return items;
	}
	protected List<Integer> items = new Vector<Integer>();
	
	/**
	 */
	public int size() {
		return users.size();
	}

	public int getMaxUserID() {
		return maxUserID;
	}
	protected int maxUserID = -1;

	public int getMaxItemID() {
		return maxItemID;
	}
	protected int maxItemID = -1;
	
	/**
	 */
	public List<List<Integer>> getByUser() {
		if (byUser == null)
			buildUserIndices();
		return byUser;

	}
	/**
	 * Rating indices organized by user.
	 */
	protected List<List<Integer>> byUser;

	/**
	 */
	public List<List<Integer>> getByItem() {
		if (byItem == null)
			buildItemIndices();
		return byItem;
	}
	/**
	 * Rating indices organized by item.
	 */
	protected List<List<Integer>> byItem;

	/**
	 */
	public Integer[] getRandomIndex() {
		if (randomIndex == null || randomIndex.length != size())
			buildRandomIndex();

		return randomIndex;
	}
	private Integer[] randomIndex;

	/**
	 */
	public Integer[] getAllUsers() {
		Set<Integer> resultSet = new HashSet<Integer>();
		for (int index = 0; index < users.size(); index++)
			resultSet.add(users.get(index));
		return resultSet.toArray(new Integer[0]);
	}

	/**
	 */
	public Integer[] getAllItems() {
		Set<Integer> resultSet = new HashSet<Integer>();
		for (int index = 0; index < items.size(); index++)
			resultSet.add(items.get(index));
		return resultSet.toArray(new Integer[0]);
	}

	/**
	 */
	public void buildUserIndices()
	{
		byUser = new Vector<List<Integer>>();
		for (int u = 0; u <= maxUserID; u++)
			byUser.add(new Vector<Integer>());

		// one pass over the data
		for (int index = 0; index < size(); index++)
			byUser.get(users.get(index)).add(index);
	}

	/**
	 */
	public void buildItemIndices()
	{
		byItem = new Vector<List<Integer>>();
		for (int i = 0; i <= maxItemID; i++)
			byItem.add(new Vector<Integer>());

		// one pass over the data
		for (int index = 0; index < size(); index++)
			byItem.get(items.get(index)).add(index);
	}

	/**
	 */
	public void buildRandomIndex()
	{
		if (randomIndex == null || randomIndex.length != size())
		{
			randomIndex = new Integer[size()];
			for (int index = 0; index < size(); index++)
				randomIndex[index] = index;
		}
		Collections.shuffle(Arrays.asList(randomIndex), Random.getInstance());
	}

	/**
	 */
	public abstract void removeUser(int user_id);

	/**
	 */
	public abstract void removeItem(int item_id);
}