package annotatorstub.main;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import annotatorstub.annotator.FakeAnnotator;
import annotatorstub.utils.Utils;
import it.unipi.di.acube.batframework.cache.BenchmarkCache;
import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.datasetPlugins.DatasetBuilder;
import it.unipi.di.acube.batframework.metrics.Metrics;
import it.unipi.di.acube.batframework.metrics.MetricsResultSet;
import it.unipi.di.acube.batframework.metrics.StrongAnnotationMatch;
import it.unipi.di.acube.batframework.metrics.StrongTagMatch;
import it.unipi.di.acube.batframework.problems.A2WDataset;
import it.unipi.di.acube.batframework.utils.DumpData;
import it.unipi.di.acube.batframework.utils.WikipediaApiInterface;

public class BenchmarkMain {
	public static void main(String[] args) throws Exception {
		WikipediaApiInterface wikiApi = WikipediaApiInterface.api();
		A2WDataset ds = DatasetBuilder.getGerdaqTrainA();
		List<HashSet<Annotation>> truthAnno = ds.getA2WGoldStandardList();
		List<HashSet<Mention>>truthMen = ds.getMentionsInstanceList();
		System.out.println("---------------------------------------Annotation");
		System.out.println("Annotation size: " + truthAnno.size());
		for(HashSet<Annotation> t : truthAnno){
			Iterator<Annotation> it = t.iterator();
			while(it.hasNext()){
				Annotation a = it.next();
				String title = WikipediaApiInterface.api().getTitlebyId(a.getConcept());
				System.out.println(title);
			}
		}
		System.out.println("---------------------------------------Mention");
		System.out.println("Mention size: " + truthMen.size());
		for(HashSet<Mention> m : truthMen){
			Iterator<Mention> men = m.iterator();
			while(men.hasNext()){
				Mention mention = men.next();
				System.out.println(mention.getPosition() + " " + mention.getLength());
			} 
		}
		System.out.println("---------------------------------------query");
		System.out.println("Query size: " + ds.getTextInstanceList().size());
		for(String str : ds.getTextInstanceList()){
			System.out.println(str);
		}
		
		
		FakeAnnotator ann = new FakeAnnotator();

		List<HashSet<Tag>> resTag = BenchmarkCache.doC2WTags(ann, ds);
		List<HashSet<Annotation>> resAnn = BenchmarkCache.doA2WAnnotations(ann, ds);
		DumpData.dumpCompareList(ds.getTextInstanceList(), ds.getA2WGoldStandardList(), resAnn, wikiApi);

		Metrics<Tag> metricsTag = new Metrics<>();
		MetricsResultSet C2WRes = metricsTag.getResult(resTag, ds.getC2WGoldStandardList(), new StrongTagMatch(wikiApi));
		Utils.printMetricsResultSet("C2W", C2WRes, ann.getName());

		Metrics<Annotation> metricsAnn = new Metrics<>();
		MetricsResultSet rsA2W = metricsAnn.getResult(resAnn, ds.getA2WGoldStandardList(), new StrongAnnotationMatch(wikiApi));
		Utils.printMetricsResultSet("A2W-SAM", rsA2W, ann.getName());
		
		Utils.serializeResult(ann, ds, new File("annotations.bin"));
		wikiApi.flush();
	}

}
