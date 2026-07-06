/* Copyright (C) 2013-2026 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package webmapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import de.learnlib.algorithm.PassiveLearningAlgorithm.PassiveDFALearner;
import de.learnlib.algorithm.PassiveLearningAlgorithm.PassiveMealyLearner;
import de.learnlib.algorithm.rpni.BlueFringeEDSMDFA;
import de.learnlib.algorithm.rpni.BlueFringeRPNIMealy;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.graph.Graph;
import net.automatalib.incremental.dfa.tree.IncrementalDFATreeBuilder;
import net.automatalib.visualization.Visualization;
import net.automatalib.word.Word;

public final class WebMapper {

    private WebMapper() {
        // prevent instantiation
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
        List<Word<String>> positiveSamples = new ArrayList<>();
        List<Word<String>> negativeSamples = new ArrayList<>();

        // Group requests into sessions by:
        // - same host (origin)
        // - each message in session within 10 seconds of the first request

        while (!requests.isEmpty()) {
            System.err.println(requests.size());
            HTTPMessage first = requests.get(0);
            requests.remove(0);
            List<String> abstracted_messages = new ArrayList<>();
            abstracted_messages.add(webInputMapper.abstract_input(first));
            alphabetSymbols.add(webInputMapper.abstract_input(first));

            String origin = first.getOrigin();
            long lastEpoch = first.getTime();
            int i = 0;
            boolean redundant = false;

            while (i < requests.size()) {
                HTTPMessage candidate = requests.get(i);
                String candidateOrigin = candidate.getUrl().split("/")[2];
                long currentEpoch = candidate.getTime();
                String abstract_candidate = webInputMapper.abstract_input(candidate);
                System.out.println(abstract_candidate);
                abstracted_messages.add(abstract_candidate);

                boolean sameOrigin = origin.equals(candidateOrigin);
                boolean withinWindow = Math.abs(currentEpoch - lastEpoch) <= 10000; // 10 sec window

                i++;
                if (sameOrigin && withinWindow) {
                    requests.remove(candidate);
                    i--;
                    alphabetSymbols.add(abstract_candidate);
                    Word<String> abstracted_messages_word = Word.fromList(abstracted_messages);
                    if (webInputClassifier.classify(candidate)) {
                        if (redundant == false){
                            redundant = true;
                        }
                        else {
                           positiveSamples.remove(positiveSamples.size()-1);
                        }
                        positiveSamples.add(abstracted_messages_word);
                    }
                    else {
                        negativeSamples.add(abstracted_messages_word);
                        break;
                    }
                }
                else if (withinWindow) continue;
                else break;
            }
        }
        // for (HTTPMessage request : requests) {
        //     String abstract_input = webInputMapper.abstract_input(request);
        //     String abstract_output = webInputMapper.abstract_output(request);

        //     // determine which duplicate is worth adding duplicates logic
        //     if (alphabetSymbols.contains(abstract_input)){
        //         System.err.println("Warning!! Duplicate found for request: " + request);
        //         continue;
        //     }
        //     alphabetSymbols.add(abstract_input);

        //     if (webInputClassifier.classify(request)){
        //         positiveSamples.add(Word.fromLetter(abstract_input));
        //     }
        //     else {
        //         negativeSamples.add(Word.fromLetter(abstract_input));
        //     }
        //     
        //     System.out.println(abstract_input);
        //     System.out.println(abstract_output);
        //     System.out.println("--------------------------------");

        final Alphabet<String> alphabet = Alphabets.fromCollection(alphabetSymbols);

        // if no training samples have been provided, only the empty automaton can be constructed
        //final DFA<?, String> emptyModel = computeModel(alphabet, Collections.emptyList(), Collections.emptyList());
        //Visualization.visualize(emptyModel, alphabet);

        // since RPNI is a greedy state-merging algorithm, providing only positive examples results in the trivial
        // one-state acceptor, because there exist no negative "counterexamples" that prevent state merges when
        // generalizing the initial prefix tree acceptor
        //final DFA<?, String> firstModel =
        //        computeModel(alphabet, positiveSamples, Collections.emptyList());
        //Visualization.visualize(firstModel, alphabet);
        
        System.out.println("Running simulation:...");
        System.out.println("---------------------------------");
        System.out.println("Positive samples: " + positiveSamples.size());
        for (Word<String> i : positiveSamples){
            System.out.println(i);
        }
        System.out.println("---------------------------------");
        System.out.println("Negative samples: " + negativeSamples.size());
        for (Word<String> i : negativeSamples){
            System.out.println(i);
        }

        final Set<Word<String>> positiveSet = new LinkedHashSet<>(positiveSamples);
        final Set<Word<String>> negativeSet = new LinkedHashSet<>(negativeSamples);
        for (Word<String> conflict : positiveSet) {
            if (negativeSet.contains(conflict)) {
                System.err.println("CONFLICT: " + conflict);
                negativeSamples.remove(conflict);
            }
        }

        // with negative samples (i.e. words that must not be accepted by the model) we get a more "realistic"
        // generalization of the given training set
        final DFA<?, String> secondModel = computeModel(alphabet, positiveSamples, negativeSamples);
        Visualization.visualize(secondModel, alphabet);
        Graph<String,String> graph = generate_Graph(alphabet, positiveSet, negativeSet);
        Visualization.visualize(graph);
    }

    private static <I> Graph<I,I> generate_Graph(Alphabet<I> alphabet,
                                              Collection<Word<I>> positiveSamples,
                                              Collection<Word<I>> negativeSamples) {
        
        IncrementalDFATreeBuilder<I> treeBuilder = new IncrementalDFATreeBuilder<>(alphabet);
        for (Word i : positiveSamples) {
            treeBuilder.insert(i, true);
        }
        for (Word i : negativeSamples) {
            treeBuilder.insert(i, false);
        }

        // get a usable view
        return (Graph<I, I>) treeBuilder.asGraph();
        // or, if you want a DFA-shaped transition system:

    }
    
    /**
     * Creates the learner instance, computes and return the inferred model.
     *
     * @param alphabet
     *         domain from which the learning data are sampled
     * @param positiveSamples
     *         positive samples
     * @param negativeSamples
     *         negative samples
     * @param <I>
     *         input symbol type
     *
     * @return the learned model
     */
    private static <I> DFA<?, I> computeModel(Alphabet<I> alphabet,
                                              Collection<Word<I>> positiveSamples,
                                              Collection<Word<I>> negativeSamples) {

        // instantiate learner
        // alternatively one can also use the EDSM variant (BlueFringeEDSMDFA from the learnlib-rpni-edsm artifact)
        // or the MDL variant (BlueFringeMDLDFA from the learnlib-rpni-mdl artifact)
        //final PassiveDFALearner<I> learner = new BlueFringeRPNIDFA<>(alphabet);
        final PassiveDFALearner<I> learner = new BlueFringeEDSMDFA<>(alphabet);

        learner.addPositiveSamples(positiveSamples);
        learner.addNegativeSamples(negativeSamples);

        return learner.computeModel();
    }

}