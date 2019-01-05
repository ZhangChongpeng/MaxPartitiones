package cn.zzp.spark;

import java.util.ArrayList;
import java.util.Collections;

import cn.zzp.graph.Edge;
import cn.zzp.graph.GetIndex;
import cn.zzp.graph.Graph;
import cn.zzp.graph.Vertex;
import scala.Tuple2;

public class MaxClique {

	/**
	 * @author zzp
	 * @throws IOException
	 */

	static int n =24; // 机器的计算节点数
	static ArrayList<Tuple2<ArrayList<Integer>, Vertex[]>> list = new ArrayList<Tuple2<ArrayList<Integer>, Vertex[]>>();

	public static void main(String[] args) throws Exception {

		long T1, T2;
		System.gc();
		// 对Vertex[]进行一个划分，划分后的子图存放到全局变量list中
		Vertex[] V = ReadFile.ReadDIMACS("../MaxPartitiones/src/main/java/cn/zzp/file/brock400_2.clq");
		T1 = System.currentTimeMillis();
		ArrayList<Tuple2<ArrayList<Integer>, Vertex[]>> list0 = new ArrayList<Tuple2<ArrayList<Integer>, Vertex[]>>();
		double originGraph = TIME.RunningTime(V);
		System.out.println("大图运行时间：" + TIME.RunningTime(V));

		ArrayList<Integer> handle = Handle.rest(V);
		ArrayList<Integer> remainList = RemainNode(n, handle, V); // 选出与机器节点数相同数目的顶点
		handle.removeAll(remainList); // 剩余节点
		for (int i = 0; i < remainList.size(); i++) {
			ArrayList<Integer> tempList = new ArrayList<Integer>();
			ArrayList<Integer> newList = new ArrayList<Integer>();
			Edge e = V[GetIndex.getVerName(remainList.get(i), V)].link;
			while (e != null) {
				tempList.add(e.verAdj);
				e = e.link;
			}
			Graph subGraph = new Graph(tempList, V);
			Vertex[] Ver = subGraph.CreateGraph();
			newList.add(remainList.get(i));
			list0.add(new Tuple2<ArrayList<Integer>, Vertex[]>(newList, Ver));
		}
		Graph subGraph = new Graph(handle, V);
		Vertex[] Vert = subGraph.CreateGraph();
		ArrayList<Integer> newList = new ArrayList<Integer>();
		list0.add(new Tuple2<ArrayList<Integer>, Vertex[]>(newList, Vert));

		double z = 0;
		for (int i = 0; i < list0.size(); i++) {
			z = z + TIME.RunningTime(list0.get(i)._2);
			System.out.print(TIME.RunningTime(list0.get(i)._2) + "   ");
		}
		System.out.println();
		System.out.println("z=" + z);

		// 调用load_balance(list,n)方法，返回机器处理完list分配给他们任务的最大时间，最小时间
		ArrayList<Double> load = load_balance(list0, n);
		double max = load.get(0);
		double min = load.get(1);
		System.out.println("max=" + max);
		System.out.println("min=" + min);

		while (!list0.isEmpty()) {
			System.out.println("------list0=" + list0.size());
			ArrayList<Tuple2<ArrayList<Integer>, Vertex[]>> list1 = new ArrayList<Tuple2<ArrayList<Integer>, Vertex[]>>();
			ArrayList<Tuple2<ArrayList<Integer>, Vertex[]>> list2 = new ArrayList<Tuple2<ArrayList<Integer>, Vertex[]>>();
			for (int i = 0; i < list0.size(); i++) {
				System.out.println(TIME.RunningTime(list0.get(i)._2));
				System.out.println(min *2);
				if (TIME.RunningTime(list0.get(i)._2) > (min * 3)) {
					System.out.println(TIME.RunningTime(list0.get(i)._2));
					ArrayList<Tuple2<ArrayList<Integer>, Vertex[]>> t = partitionAlgorithm(list0.get(i), min * 3);
					list2.add(list0.get(i));
					list1.addAll(t);
				} else {
					System.out.println("\\\\\\\\\\");
					list.add(list0.get(i));
				}
			}

			System.out.println("需要划分的图的个数" + list2.size());
			System.out.println("新加图的个数" + list1.size());
			System.out.println(list0.size());
			list0.removeAll(list2);
			list0.removeAll(list);
			list0.addAll(list1);
			System.out.println(list0.size());
			System.out.println("list0=" + list0.size());
			System.out.println("list=" + list.size());
//			break;
		}

		System.out.println("-------------------------");
		int k = 0;
		for (int i = 0; i < list.size(); i++) {
			k++;
			System.out.print(TIME.RunningTime(list.get(i)._2) + "  ");
			if (k % 10 == 0)
				System.out.println();
		}
		System.out.println("-------------------------");

		// 调用load_balance(list,n)方法，返回集群节点处理完list分配给他们的最大时间，最小时间以及平均时间
		ArrayList<Double> load1 = load_balance(list, n);
		double max1 = load1.get(0);
		double min1 = load1.get(1);
		System.out.println("max1=" + max1);
		System.out.println("min2=" + min1);
		System.out.println((max1 - min1) / max1);

		while ((max1 - min1) / max1 >= 0.1) {
			// 遍历list所有子图，找出一个运行时间最小的子图
			double k1 = TIME.RunningTime(list.get(0)._2);
			System.out.println("初始k1=" + k1);
			for (int i = 1; i < list.size(); i++) {
				if (TIME.RunningTime(list.get(i)._2) < k1) {
					k1 = TIME.RunningTime(list.get(i)._2);
				}
			}
			System.out.println("k1=" + k1);

			ArrayList<Tuple2<ArrayList<Integer>, Vertex[]>> listn = new ArrayList<Tuple2<ArrayList<Integer>, Vertex[]>>();
			listn.addAll(list);
			list.removeAll(list);

			while (!listn.isEmpty()) {
				System.out.println("------listn=" + listn.size());
				ArrayList<Tuple2<ArrayList<Integer>, Vertex[]>> list1 = new ArrayList<Tuple2<ArrayList<Integer>, Vertex[]>>();
				ArrayList<Tuple2<ArrayList<Integer>, Vertex[]>> list2 = new ArrayList<Tuple2<ArrayList<Integer>, Vertex[]>>();
				for (int i = 0; i < listn.size(); i++) {
					System.out.println(TIME.RunningTime(listn.get(i)._2));
					System.out.println(k1 * 2);
					if (TIME.RunningTime(listn.get(i)._2) > (k1 * 3)) {
						System.out.println(TIME.RunningTime(listn.get(i)._2));
						ArrayList<Tuple2<ArrayList<Integer>, Vertex[]>> t = partitionAlgorithm(listn.get(i), k1 * 3);
						list2.add(listn.get(i));
						list1.addAll(t);
					} else {
						System.out.println("\\\\\\\\\\");
						list.add(listn.get(i));
					}
				}

				System.out.println("需要划分的图的个数" + list2.size());
				System.out.println("新加图的个数" + list1.size());
				System.out.println(listn.size());
				listn.removeAll(list2);
				listn.removeAll(list);
				listn.addAll(list1);
				System.out.println(listn.size());
				System.out.println("listn=" + listn.size());
				System.out.println("list=" + list.size());
//				break;
			}

			System.out.println("-------------------------");
			int kk = 0;
			for (int i = 0; i < list.size(); i++) {
				kk++;
				System.out.print(TIME.RunningTime(list.get(i)._2) + "  ");
				if (kk % 10 == 0)
					System.out.println();
			}
			System.out.println("-------------------------");

			// 调用load_balance(list,n)方法，返回集群节点处理完list分配给他们的最大时间，最小时间以及平均时间
			ArrayList<Double> load2 = load_balance(list, n);
			max1 = load2.get(0);
			min1 = load2.get(1);
			System.out.println("max1=" + max1);
			System.out.println("min1=" + min1);
			System.out.println((max1 - min1) / max1);

//			break;

		}

		// 输出子图数量
		System.out.println("The number of graph:" + list.size());
		T2 = System.currentTimeMillis();
		System.out.println("Partition RunningTime:" + (T2 - T1) + "ms");
	}

	static double ALLTINE(ArrayList<Tuple2<ArrayList<Integer>, Vertex[]>> list1) {
		double z = 0;
		for (int i = 0; i < list1.size(); i++) {
			z = z + TIME.RunningTime(list.get(i)._2);
		}
		return z;
	}

	static ArrayList<Integer> RemainNode(int n, ArrayList<Integer> handle, Vertex[] V) {
		ArrayList<Integer> newList = new ArrayList<Integer>();
		for (int i = 0; i < n - 1; i++) {
			newList.add(handle.get(i));
		}
		return newList;
	}

	// 分区算法
	static ArrayList<Tuple2<ArrayList<Integer>, Vertex[]>> partitionAlgorithm(Tuple2<ArrayList<Integer>, Vertex[]> t,
			double min) {
		// 对t._2进行降序排序，着色，着色后升序排序得到顶点集合verList
		ArrayList<Tuple2<ArrayList<Integer>, Vertex[]>> partitionList = new ArrayList<Tuple2<ArrayList<Integer>, Vertex[]>>();
		ArrayList<Integer> verList = Handle.rest(t._2);
		Vertex[] vert;
		do {
			ArrayList<Integer> tempList = new ArrayList<Integer>();
			Edge ed = t._2[GetIndex.getVerName(verList.get(0), t._2)].link;
			while (ed != null) {
				tempList.add(ed.verAdj);
				ed = ed.link;
			}
			Graph subGraph = new Graph(tempList, t._2);
			Vertex[] Ver = subGraph.CreateGraph();
			ArrayList<Integer> newList = new ArrayList<Integer>();
			newList.addAll(t._1);
			newList.add(verList.get(0));
			verList.remove(0);
			partitionList.add(new Tuple2<ArrayList<Integer>, Vertex[]>(newList, Ver));
			Graph remainGraph = new Graph(verList, t._2);
			vert = remainGraph.CreateGraph();
			System.out.println("+" + TIME.RunningTime(vert));
		} while (TIME.RunningTime(vert) > min);
		ArrayList<Integer> newList = new ArrayList<Integer>();
		newList.addAll(t._1);
		partitionList.add(new Tuple2<ArrayList<Integer>, Vertex[]>(newList, vert));
		return partitionList;
	}

	// 负载均衡模拟算法
	static ArrayList<Double> load_balance(ArrayList<Tuple2<ArrayList<Integer>, Vertex[]>> list, int n) {
		ArrayList<Double> timeList = new ArrayList<Double>();
		double sum = 0;
		for (int i = 0; i < list.size(); i++) {
			timeList.add(TIME.RunningTime(list.get(i)._2));
			sum = sum + TIME.RunningTime(list.get(i)._2);
		}
		ArrayList<Double> t = new ArrayList<Double>(n);
		for (int i = 0; i < n; i++) {
			t.add(timeList.get(i));
		}
		for (int i = n; i < timeList.size(); i++) {
			// 找出double数组中的最小值

			double temp = Collections.min(t);
			t.set(t.indexOf(temp), t.get(t.indexOf(temp)) + timeList.get(i));
		}
		// 遍历t[n]，找出最大值，最小值以及平均值
		double max = Collections.max(t);
		double min = Collections.min(t);

		ArrayList<Double> result = new ArrayList<Double>();
		result.add(max);
		result.add(min);
		return result;
	}
}