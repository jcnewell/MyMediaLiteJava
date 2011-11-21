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
		if (by_user == null)
			BuildUserIndices();
		return by_user;

	}
	/**
	 * Rating indices organized by user.
	 */
	protected List<List<Integer>> by_user;

	/**
	 */
	public List<List<Integer>> getByItem() {
		if (by_item == null)
			BuildItemIndices();
		return by_item;
	}
	/**
	 * Rating indices organized by item.
	 */
	protected List<List<Integer>> by_item;

	/**
	 */
	public Integer[] getRandomIndex() {
		if (random_index == null || random_index.length != size())
			BuildRandomIndex();

		return random_index;
	}
	private Integer[] random_index;

	/**
	 */
	public Integer[] getAllUsers() {
		Set<Integer> result_set = new HashSet<Integer>();
		for (int index = 0; index < users.size(); index++)
			result_set.add(users.get(index));
		return result_set.toArray(new Integer[0]);
	}

	/**
	 */
	public Integer[] getAllItems() {
		Set<Integer> result_set = new HashSet<Integer>();
		for (int index = 0; index < items.size(); index++)
			result_set.add(items.get(index));
		return result_set.toArray(new Integer[0]);
	}

	/**
	 */
	public void BuildUserIndices()
	{
		by_user = new Vector<List<Integer>>();
		for (int u = 0; u <= maxUserID; u++)
			by_user.add(new Vector<Integer>());

		// one pass over the data
		for (int index = 0; index < size(); index++)
			by_user.get(users.get(index)).add(index);
	}

	/**
	 */
	public void BuildItemIndices()
	{
		by_item = new Vector<List<Integer>>();
		for (int i = 0; i <= maxItemID; i++)
			by_item.add(new Vector<Integer>());

		// one pass over the data
		for (int index = 0; index < size(); index++)
			by_item.get(items.get(index)).add(index);
	}

	/**
	 */
	public void BuildRandomIndex()
	{
		if (random_index == null || random_index.length != size())
		{
			random_index = new Integer[size()];
			for (int index = 0; index < size(); index++)
				random_index[index] = index;
		}
		Collections.shuffle(Arrays.asList(random_index), Random.getInstance());
	}

	/**
	 */
	public abstract void RemoveUser(int user_id);

	/**
	 */
	public abstract void RemoveItem(int item_id);
}