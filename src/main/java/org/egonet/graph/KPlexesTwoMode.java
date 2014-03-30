package org.egonet.graph;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class KPlexesTwoMode<N> {
	// graph is Map<N,Set<N>> plus Set<N> to indicate which are in first mode
	// kplex is <Set<N>>
	public Map<N,Integer> connectionsByNode(Map<N,Set<N>> graph) {
		Map<N,Integer> result = Maps.newHashMap();
		for(N n : graph.keySet()) {
			result.put(n, graph.get(n).size());
		}
		return result;
	}
	// This is similar to hIndex.
	// Connectedness of N for having N connections to nodes with N connections.
	public Map<N,Integer> connectednessByNode(Map<N,Set<N>> graph) {
		Map<N,Integer> connectionsByNode = connectionsByNode(graph);
		Map<N,Integer> results = Maps.newHashMap();
		for(N n1 : graph.keySet()) {
			List<Integer> connectednessOfConnections = Lists.newArrayList();
			for(N n2 : graph.get(n1)) {
				connectednessOfConnections.add(connectionsByNode.get(n2));
			}
			Collections.sort(connectednessOfConnections);
			Collections.reverse(connectednessOfConnections);
			Integer result = 0;
			for(Integer i = 0; i < connectednessOfConnections.size(); i++) {
				if(i < connectednessOfConnections.get(i)) {
					result = i+1;
				}
			}
			results.put(n1, result);
		}
		return results;
	}
	public Map<N,Integer> connectionsWithinSubgroup(Map<N,Set<N>> graph, Set<N> subgroup) {
		Map<N,Integer> result = Maps.newHashMap();
		for(N n : graph.keySet()) {
			result.put(n, Sets.intersection(graph.get(n),subgroup).size());
		}
		return result;
	}
	public Integer highestConnectedness(Map<N,Set<N>> graph) {
		Integer highest = 0;
		for(Integer connectedness : connectednessByNode(graph).values()) {
			if(connectedness > highest) {
				highest = connectedness;
			}
		}
		return highest;
	}
	public Set<N> meetConnectednessThreshold(Map<N,Set<N>> graph, Integer k, Integer targetSize) {
		Integer threshold = targetSize - k;
		Set<N> result = Sets.newHashSet();
		Map<N,Integer> connectedness = connectednessByNode(graph);
		for(N n : graph.keySet()) {
			if(connectedness.get(n) > threshold-1) {
				result.add(n);
			}
		}
		return result;
	}
	public Integer largestPossibleKPlex(Map<N,Set<N>> graph, Integer k) {
		return highestConnectedness(graph)+k;
	}
	public Set<N> criticalNodesInKPlex(Map<N,Set<N>> graph, Set<N> mode1, Set<N> kplex, Integer k) {
		if(! graph.keySet().containsAll(kplex)) {
			throw new RuntimeException("The kplex "+kplex+" is not a subset of the graph "+graph);
		}
		Integer mode1KPlexSize = Sets.intersection(mode1, kplex).size();
		Integer mode2KPlexSize = kplex.size() - mode1KPlexSize;
		Map<N,Integer> connectionsWithinKPlex = connectionsWithinSubgroup(graph,kplex);
		Set<N> criticalNodes = Sets.newHashSet();
		for(N n : kplex) {
			Integer connections = connectionsWithinKPlex.get(n);
			if(connections == null) {
				throw new RuntimeException("null connections for "+n+" in "+kplex+" of graph "+graph);
			}
			Integer otherModeSize = (mode1.contains(n) ? mode2KPlexSize : mode1KPlexSize);
			Integer threshold = otherModeSize-k;
			if(connections < threshold+1) {
				criticalNodes.add(n);
			}
		}
		return criticalNodes;
	}
	
	public Set<N> nodesThatCanBeAddedToKPlex(Map<N,Set<N>> graph, Set<N> mode1, Set<N> kplex, Integer k) {
		if(kplex.isEmpty()) {
			if(k > 0) {
				return graph.keySet();
			} else {
				return new HashSet<N>();
			}
		}
		Set<N> criticalNodes = criticalNodesInKPlex(graph, mode1, kplex, k);
		Set<N> outsideKPlex = Sets.difference(graph.keySet(),kplex);
		Set<N> mode1Eligible = Sets.intersection(mode1, outsideKPlex);
		Set<N> mode2Eligible = Sets.difference(outsideKPlex, mode1Eligible);
		for(N n : criticalNodes) {
			// Must be neighbor of all critical nodes in other mode.
			if(mode1.contains(n)) {
				mode2Eligible = Sets.intersection(mode2Eligible, graph.get(n));
			} else {
				mode1Eligible = Sets.intersection(mode1Eligible, graph.get(n));
			}
		}
		Set<N> result = Sets.newHashSet();
		Map<N,Integer> connections = connectionsWithinSubgroup(graph,kplex);
		Integer kplexMode1Size = Sets.intersection(kplex, mode1).size();
		Integer kplexMode2Size = kplex.size()-kplexMode1Size;
		for(N n : Sets.union(mode1Eligible, mode2Eligible)) {
			if(connections.get(n) > (mode1.contains(n) ? kplexMode2Size : kplexMode1Size)-k-1)
			{
				result.add(n);
			}
		}
		return result;
	}
	public Map<N,Set<N>> createSubgraph(Map<N,Set<N>> graph, Set<N> nodes) {
		Map<N,Set<N>> subgraph = Maps.newHashMap();
		for(N n : nodes) {
			subgraph.put(n, Sets.intersection(graph.get(n),nodes));
		}
		return subgraph;
	}
	public Set<N> neighborsOfSubgroup(Map<N,Set<N>> graph, Set<N> subgroup) {
		Map<N,Integer> connections = connectionsWithinSubgroup(graph, subgroup);
		Set<N> results = Sets.newHashSet();
		for(N n : graph.keySet()) {
			if(connections.get(n) > 0 && !subgroup.contains(n)) {
				results.add(n);
			}
		}
		return results;
	}
	public Map<N,Set<N>> subgraphBoundingFinalKPlex(
			Map<N,Set<N>> graph, Set<N> mode1, Set<N> kplex, Integer k, Integer targetSize)
	{
		Set<N> includeInSubgraph =
			Sets.union(kplex,
					Sets.intersection(
							meetConnectednessThreshold(graph,k,targetSize), 
							nodesThatCanBeAddedToKPlex(graph,mode1,kplex,k)));
		return createSubgraph(graph,includeInSubgraph);
	}
	public N chooseNodeForInclusionInKPlex(Map<N,Set<N>> graph, Set<N> mode1, Set<N> kplex, Integer k) {
		Integer highScore = 0;
		N choice = null;
		Map<N,Integer> connectedness = connectednessByNode(graph);
		Map<N,Integer> connectionsWithinKPlex = connectionsWithinSubgroup(graph,kplex);
		Set<N> addable = nodesThatCanBeAddedToKPlex(graph, mode1, kplex, k);
		if(!kplex.isEmpty()) {
			addable = Sets.intersection(addable, neighborsOfSubgroup(graph,kplex));
		}
		Integer alreadyInMode1 = Sets.intersection(mode1, kplex).size();
		Integer alreadyInMode2 = kplex.size()-alreadyInMode1;
		for(N n : addable) {
			Integer score = connectedness.get(n) + 5*connectionsWithinKPlex.get(n)
				+ 5*(mode1.contains(n) ? alreadyInMode2 : alreadyInMode1);
			if(score > highScore) {
				highScore = score;
				choice = n;
			}
		}
		return choice;
	}
	public Set<N> growKPlex(Map<N,Set<N>> graph, Set<N> mode1, Set<N> kplex, Integer k, Integer targetSize) {
		Map<N,Set<N>> boundingGraph = subgraphBoundingFinalKPlex(graph, mode1,kplex,k,targetSize);
		N newNode = chooseNodeForInclusionInKPlex(boundingGraph, mode1,kplex,k);
		if(newNode == null) {
			return kplex;
		}
		Set<N> newKPlex = Sets.newHashSet();
		newKPlex.add(newNode);
		newKPlex.addAll(kplex);
		Integer actualk = maxMissingEdgesPerNodeInSubgroup(graph,mode1,newKPlex);
		if(actualk > k) {
			String msg = "After adding "+newNode+", found k-plex with k that is too high. "+
			actualk+" instead of "+k+": "+newKPlex;
			System.out.println(msg);
			for(N n : newKPlex) {
				System.out.println(n+" : "+Sets.intersection(graph.get(n), newKPlex));
			}
			throw new RuntimeException(msg);
		}
		return growKPlex(boundingGraph, mode1,newKPlex,k,targetSize);
	}
	public Set<N> findLargeKPlex(Map<N,Set<N>> graph, Set<N> mode1, Integer k) {
		Set<N> largestFound = Sets.newHashSet();
		Integer mostInSmallMode = 0;
		Integer mostInLargeMode = 0;
		for(Integer targetSize = largestPossibleKPlex(graph, k); 
			targetSize > mostInSmallMode; 
			targetSize--)
		{
			Set<N> seeds = meetConnectednessThreshold(graph, k, targetSize);
			Map<N,Set<N>> boundedGraph = createSubgraph(graph,seeds);
			for(N seed : seeds) {
				Set<N> kplex = Sets.newHashSet();
				kplex.add(seed);
				kplex = growKPlex(boundedGraph,mode1,kplex,k,targetSize);
				Set<N> inMode1 = Sets.intersection(kplex,mode1);
				Set<N> inMode2 = Sets.difference(kplex, inMode1);
				Integer inSmallMode = Math.min(inMode1.size(), inMode2.size());
				Integer inLargeMode = Math.max(inMode1.size(), inMode2.size());
				if(inSmallMode > mostInSmallMode ||
						(inSmallMode.equals(mostInSmallMode) && inLargeMode > mostInLargeMode)) 
				{
					largestFound = kplex;
					mostInSmallMode = inSmallMode;
					mostInLargeMode = inLargeMode;
				}
			}
		}
		Integer actualk = maxMissingEdgesPerNodeInSubgroup(graph,mode1,largestFound);
		if(actualk > k) {
			throw new RuntimeException("Found k-plex with k that is too high. "+
					actualk+" instead of "+k+": "+largestFound);
		}
		return largestFound;
	}
	public Integer maxMissingEdgesPerNodeInSubgroup(
			Map<N,Set<N>> graph, Set<N> mode1, Set<N> subgroup) 
	{
		Integer maxMissing = 0;
		Set<N> submode1 = Sets.intersection(mode1, subgroup);
		Set<N> submode2 = Sets.difference(subgroup, submode1);
		for(N n1 : subgroup) {
			Set<N> otherSubmode = mode1.contains(n1) ? submode2 : submode1;
			Integer edges = Sets.intersection(otherSubmode, graph.get(n1)).size();
			Integer missing = otherSubmode.size()-edges;
			if(missing > maxMissing) {
				maxMissing = missing;
			}
			if(missing < 0) {
				throw new RuntimeException("Can't be missing negative number of edges.");
			}
		}
		return maxMissing;
	}
}
