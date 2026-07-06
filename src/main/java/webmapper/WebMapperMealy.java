package webmapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.javatuples.Pair;

import de.learnlib.algorithm.PassiveLearningAlgorithm.PassiveMealyLearner;
import de.learnlib.algorithm.rpni.BlueFringeRPNIMealy;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.visualization.Visualization;
import net.automatalib.word.Word;

public final class WebMapperMealy {

    private WebMapperMealy() {
        // prevent instantiation
    }

    // Splits an abstracted (input, output) trace into just the input symbols.
    private static Word<String> toInputWord(Word<Pair<String, String>> trace) {
        List<String> ins = new ArrayList<>(trace.length());
        for (Pair<String, String> step : trace) {
            ins.add(step.getValue0());
        }
        return Word.fromList(ins);
    }

    // Splits an abstracted (input, output) trace into just the output symbols.
    private static Word<String> toOutputWord(Word<Pair<String, String>> trace) {
        List<String> outs = new ArrayList<>(trace.length());
        for (Pair<String, String> step : trace) {
            outs.add(step.getValue1());
        }
        return Word.fromList(outs);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java WebMapper <corpus_file>");
            System.exit(1);
        }
        String corpus_file = args[0];
        CorpusParser corpusParser = new CorpusParser();
        ArrayList<HTTPMessage> requests = corpusParser.parse_corpus(corpus_file);
        WebInputMapper webInputMapper = new WebInputMapper();
        WebInputClassifier webInputClassifier = new WebInputClassifier();

        Set<String> alphabetSymbols = new LinkedHashSet<>();

        // FIX: these need to hold whole traces (Word<Pair<input,output>>),
        // not single Pair<String,String> entries.
        List<Word<Pair<String, String>>> positiveSamples = new ArrayList<>();
        List<Word<Pair<String, String>>> negativeSamples = new ArrayList<>();

        while (!requests.isEmpty()) {
            System.err.println(requests.size());
            HTTPMessage first = requests.get(0);
            requests.remove(0);
            List<Pair<String, String>> abstracted_messages = new ArrayList<>();
            abstracted_messages.add(new Pair<>(webInputMapper.abstract_input(first), webInputMapper.abstract_output(first)));
            alphabetSymbols.add(webInputMapper.abstract_input(first));

            String origin = first.getOrigin();
            long lastEpoch = first.getTime();
            int i = 0;
            boolean redundant = false;

            while (i < requests.size()) {
                HTTPMessage candidate = requests.get(i);
                String candidateOrigin = candidate.getUrl().split("/")[2];
                long currentEpoch = candidate.getTime();
                Pair<String, String> abstract_candidate =
                        new Pair<>(webInputMapper.abstract_input(candidate), webInputMapper.abstract_output(candidate));
                abstracted_messages.add(abstract_candidate);

                boolean sameOrigin = origin.equals(candidateOrigin);
                boolean withinWindow = Math.abs(currentEpoch - lastEpoch) <= 10000; // 10 sec window

                i++;
                if (sameOrigin && withinWindow) {
                    requests.remove(candidate);
                    i--;
                    alphabetSymbols.add(abstract_candidate.getValue0());
                    Word<Pair<String, String>> abstracted_messages_word = Word.fromList(abstracted_messages);
                    if (webInputClassifier.classify(candidate)) {
                        if (!redundant) {
                            redundant = true;
                        } else {
                            positiveSamples.remove(positiveSamples.size() - 1);
                        }
                        positiveSamples.add(abstracted_messages_word);
                    } else {
                        negativeSamples.add(abstracted_messages_word);
                        break;
                    }
                } else if (withinWindow) {
                    continue;
                } else {
                    break;
                }
            }
        }

        final Alphabet<String> alphabet = Alphabets.fromCollection(alphabetSymbols);
        final PassiveMealyLearner<String, String> learner = new BlueFringeRPNIMealy<>(alphabet);

        // FIX: split each trace into its input word and output word,
        // and feed both via addSample(Word<I>, Word<O>).
        // Note: unlike DFA-RPNI, Mealy learning has no separate
        // "positive"/"negative" sample notion — every trace is just an
        // (input, output) observation. Success/failure is expected to
        // already be encoded in the output symbols themselves (via
        // abstract_output/classify), so both lists get merged here.
        for (Word<Pair<String, String>> trace : positiveSamples) {
            learner.addSample(toInputWord(trace), toOutputWord(trace));
        }
        for (Word<Pair<String, String>> trace : negativeSamples) {
            learner.addSample(toInputWord(trace), toOutputWord(trace));
        }

        MealyMachine<?, String, ?, String> model = learner.computeModel();

        Visualization.visualize(model, alphabet);
    }
}