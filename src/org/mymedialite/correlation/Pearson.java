// Copyright (C) 2010, 2011 Zeno Gantner, Chris Newell
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

package org.mymedialite.correlation;

import java.util.HashSet;
import java.util.List;

import org.mymedialite.data.IRatings;
import org.mymedialite.datatype.Pair;
import org.mymedialite.datatype.SparseMatrix;
import org.mymedialite.taxonomy.EntityType;

/**
 * Correlation class for Pearson correlation.
 * http://en.wikipedia.org/wiki/Pearson_correlation
 */

public class Pearson extends RatingCorrelationMatrix {

	/**
	 * Shrinkage parameter.
	 */
	public float shrinkage = 10;

	/**
	 * Constructor. Create a Pearson correlation matrix.
	 * @param num_entities the number of entities
	 */
	public Pearson(int num_entities) {
		super(num_entities);
	}

	/**
	 * Create a Pearson correlation matrix from given data.
	 * @param ratings the ratings data
	 * @param entity_type the entity type, either USER or ITEM
	 * @param shrinkage a shrinkage parameter
	 * @return the complete Pearson correlation matrix
	 */
	public static CorrelationMatrix create(IRatings ratings, int entity_type, float shrinkage) {
		Pearson cm;
		int num_entities = 0;
		if (entity_type == EntityType.USER) {
			num_entities = ratings.getMaxUserID() + 1;
		} else if (entity_type == EntityType.ITEM) {
			num_entities = ratings.getMaxItemID() + 1;
		} else {
			throw new IllegalArgumentException("Unknown entity type: " + entity_type);
		}

		try {
			cm = new Pearson(num_entities);
		} catch (OutOfMemoryError e) {
			System.err.println("Too many entities: " + num_entities);
			throw e;
		}
		cm.shrinkage = shrinkage;
		cm.computeCorrelations(ratings, entity_type);
		return cm;
	}

	/**
	 * Compute correlations between two entities for given ratings.
	 * @param ratings the rating data
	 * @param entity_type the entity type, either USER or ITEM
	 * @param i the ID of first entity
	 * @param j the ID of second entity
	 * @param shrinkage the shrinkage parameter
	 */
	public static float computeCorrelation(IRatings ratings, int entity_type, int i, int j, float shrinkage) {
		if (i == j) return 1;

		List<Integer> ratings1 = (entity_type == EntityType.USER) ? ratings.getByUser().get(i) : ratings.getByItem().get(i);
		List<Integer> ratings2 = (entity_type == EntityType.USER) ? ratings.getByUser().get(j) : ratings.getByItem().get(j);

		// get common ratings for the two entities
		HashSet<Integer> e1 = (entity_type == EntityType.USER) ? ratings.getItems(ratings1) : ratings.getUsers(ratings1);
		HashSet<Integer> e2 = (entity_type == EntityType.USER) ? ratings.getItems(ratings2) : ratings.getUsers(ratings2);

		e1.retainAll(e2);

		int n = e1.size();
		if (n < 2)
			return 0;

		// Single-pass variant
		double i_sum = 0;
		double j_sum = 0;
		double ij_sum = 0;
		double ii_sum = 0;
		double jj_sum = 0;
		for (int other_entity_id : e1) {

			// Get ratings
			double r1 = 0;
			double r2 = 0;
			if (entity_type == EntityType.USER) {
				r1 = ratings.get(i, other_entity_id, ratings1);
				r2 = ratings.get(j, other_entity_id, ratings2);
			} else {
				r1 = ratings.get(other_entity_id, i, ratings1);
				r2 = ratings.get(other_entity_id, j, ratings2);
			}

			// Update sums
			i_sum  += r1;
			j_sum  += r2;
			ij_sum += r1 * r2;
			ii_sum += r1 * r1;
			jj_sum += r2 * r2;
		}

		double denominator = Math.sqrt( (n * ii_sum - i_sum * i_sum) * (n * jj_sum - j_sum * j_sum) );
		if (denominator == 0) return 0;
		double pmcc = (n * ij_sum - i_sum * j_sum) / denominator;
		return (float) pmcc * (n / (n + shrinkage));
	}

	/**
	 * Compute correlations for given ratings.
	 * @param ratings the rating data
	 * @param entity_type the entity type, either USER or ITEM
	 */
	public void computeCorrelations(IRatings ratings, int entity_type) {

		if (entity_type != EntityType.USER && entity_type != EntityType.ITEM) throw new IllegalArgumentException("entity type must be either USER or ITEM, not " + entity_type);

		List<List<Integer>> ratings_by_other_entity = (entity_type == EntityType.USER) ? ratings.getByItem() : ratings.getByUser();

		SparseMatrix<Integer> freqs  = new SparseMatrix<Integer>(num_entities, num_entities);
		SparseMatrix<Double> i_sums  = new SparseMatrix<Double>(num_entities, num_entities);
		SparseMatrix<Double> j_sums  = new SparseMatrix<Double>(num_entities, num_entities);
		SparseMatrix<Double> ij_sums = new SparseMatrix<Double>(num_entities, num_entities);
		SparseMatrix<Double> ii_sums = new SparseMatrix<Double>(num_entities, num_entities);
		SparseMatrix<Double> jj_sums = new SparseMatrix<Double>(num_entities, num_entities);

		for (List<Integer> other_entity_ratings : ratings_by_other_entity) {
			for (int i = 0; i < other_entity_ratings.size(); i++) {
				int index1 = other_entity_ratings.get(i);
				int x = (entity_type == EntityType.USER) ? ratings.getUsers().get(index1) : ratings.getItems().get(index1);

				// Update pairwise scalar product and frequency
				for (int j = i + 1; j < other_entity_ratings.size(); j++) {

					int index2 = other_entity_ratings.get(j);
					int y = (entity_type == EntityType.USER) ? ratings.getUsers().get(index2) : ratings.getItems().get(index2);

					double rating1 = ratings.get(index1);
					double rating2 = ratings.get(index2);

					// Update sums
					if (x < y) {
						freqs.set(x, y, freqs.get(x, y) + 1);
						i_sums.set(x, y, i_sums.get(x, y) + rating1);
						j_sums.set(x, y, j_sums.get(x, y) + rating2);
						ij_sums.set(x, y, ij_sums.get(x, y) + rating1 * rating2);
						ii_sums.set(x, y, ii_sums.get(x, y) + rating1 * rating1);
						jj_sums.set(x, y, jj_sums.get(x, y) + rating2 * rating2);
					} else {
						freqs.set(y, x, freqs.get(y, x) + 1);
						i_sums.set(y, x, i_sums.get(y, x) + rating1);
						j_sums.set(y, x, j_sums.get(y, x) + rating2);
						ij_sums.set(y, x, ij_sums.get(y, x) + rating1 * rating2);
						ii_sums.set(y, x, ii_sums.get(y, x) + rating1 * rating1);
						jj_sums.set(y, x, jj_sums.get(y, x) + rating2 * rating2);
					}
				}
			}
		}

		// The diagonal of the correlation matrix
		for (int i = 0; i < num_entities; i++) {
			set(i, i, 1.0F);
		}

		// Fill the entries with interactions
		for (Pair<Integer, Integer> index_pair : freqs.getNonEmptyEntryIDs()) {
			int i = index_pair.first;
			int j = index_pair.second;
			int n = freqs.get(i, j);

			if (n < 2) {
				set(i, j, 0.0F);
				continue;
			}

			double numerator = ij_sums.get(i, j) * n - i_sums.get(i, j) * j_sums.get(i, j);

			double denominator = Math.sqrt( (n * ii_sums.get(i, j) - i_sums.get(i, j) * i_sums.get(i, j)) * (n * jj_sums.get(i, j) - j_sums.get(i, j) * j_sums.get(i, j)) );
			if (denominator == 0) {
				this.set(i, j, 0.0F);
				continue;
			}

			double pmcc = numerator / denominator;
			this.set(i, j, (float) (pmcc * (n / (n + shrinkage))));
		}
	}
}
