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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import de.learnlib.algorithm.PassiveLearningAlgorithm.PassiveDFALearner;
import de.learnlib.algorithm.rpni.BlueFringeRPNIDFA;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.visualization.Visualization;
import net.automatalib.word.Word;

import webmapper.CorpusParser;
import webmapper.WebInputMapper;
import webmapper.XMLRequest;

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
        ArrayList<XMLRequest> requests = corpusParser.parse_corpus(corpus_file);
        WebInputMapper webInputMapper = new WebInputMapper();

        Set<String> alphabetSymbols = new LinkedHashSet<>();
        Collection<Word<String>> positiveSamples = new ArrayList<>();
        Collection<Word<String>> negativeSamples = new ArrayList<>();

        for (XMLRequest request : requests) {
            String abstract_input = webInputMapper.abstract_input(request);
            String abstract_output = webInputMapper.abstract_output(request);
            alphabetSymbols.add(abstract_input);

            if ("200".equals(request.getStatus())) {
                positiveSamples.add(Word.fromLetter(abstract_input));
            } else {
                negativeSamples.add(Word.fromLetter(abstract_input));
            }
            
            System.out.println(abstract_input);
            System.out.println(abstract_output);
            System.out.println("--------------------------------");
        }

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

        // with negative samples (i.e. words that must not be accepted by the model) we get a more "realistic"
        // generalization of the given training set
        final DFA<?, String> secondModel =
                computeModel(alphabet, positiveSamples, negativeSamples);
        Visualization.visualize(secondModel, alphabet);
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
        final PassiveDFALearner<I> learner = new BlueFringeRPNIDFA<>(alphabet);

        learner.addPositiveSamples(positiveSamples);
        learner.addNegativeSamples(negativeSamples);

        return learner.computeModel();
    }

}