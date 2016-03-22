package annotatorstub.annotator;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.data.ScoredAnnotation;
import it.unipi.di.acube.batframework.data.ScoredTag;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.problems.Sa2WSystem;
import it.unipi.di.acube.batframework.utils.AnnotationException;
import it.unipi.di.acube.batframework.utils.ProblemReduction;
import it.unipi.di.acube.batframework.utils.WikipediaApiInterface;

public class FakeAnnotator implements Sa2WSystem {
	private static long lastTime = -1;
	private static float threshold = -1f;
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public long getLastAnnotationTime() {
		return lastTime;
	}

	public HashSet<Tag> solveC2W(String text) throws AnnotationException {
		return ProblemReduction.A2WToC2W(solveA2W(text));
	}

	public HashSet<Annotation> solveD2W(String text, HashSet<Mention> mentions) throws AnnotationException {
		return ProblemReduction.Sa2WToD2W(solveSa2W(text), mentions, threshold);
	}

	public HashSet<Annotation> solveA2W(String text) throws AnnotationException {
		return ProblemReduction.Sa2WToA2W(solveSa2W(text), threshold);
	}

	public HashSet<ScoredTag> solveSc2W(String text) throws AnnotationException {
		return ProblemReduction.Sa2WToSc2W(solveSa2W(text));
	}

	public HashSet<ScoredAnnotation> solveSa2W(String text) throws AnnotationException {
		lastTime = System.currentTimeMillis();
		List<Integer> Start = new ArrayList<Integer>();
		List<Integer> End = new ArrayList<Integer>();
		int start = 0;
		int end = start;
		while (true) {
			while (start < text.length() && !Character.isAlphabetic(text.charAt(start)))
				start++;
			end = start;
			while (end < text.length() && Character.isAlphabetic(text.charAt(end)))
				end++;
			if (end <= text.length()) {
				Start.add(start);
				End.add(end);
				start = end;
				if(end == text.length())
					break;
			}
		}

		HashSet<ScoredAnnotation> result = new HashSet<>();
		for (int i = 0; i < Start.size(); i++) {
			for (int j = i; j < Start.size(); j++) {
				int wid = 0;
				try {
					wid = WikipediaApiInterface.api().getIdByTitle(text.substring(Start.get(i), End.get(j)));
				} catch (IOException e) {
					throw new AnnotationException(e.getMessage());
				}
				if (wid != -1)
					result.add(new ScoredAnnotation(Start.get(i), End.get(j) - Start.get(i), wid, 0.1f));
			}
		}

		lastTime = System.currentTimeMillis() - lastTime;
		return result;
	}

	public String getName() {
		return "Simple yet uneffective query annotator";
	}
}
