package com.pactum.chat.mindmup

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.pactum.chat.mindmup.MindMupResponse.Idea
import com.pactum.chat.mindmup.MindMupResponse.Link
import com.pactum.chat.PatternIdInvalidException
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.SortedMap

@Service
class MindMupService(
  private var mindMupClient: MindMupClient
) {

  fun getMup(flowId: String): MindMupResponse {
    val mindMupResponse = mindMupClient.getMup(flowId)
    return expandMinMup(mindMupResponse)
  }

  private fun expandMinMup(mindMupResponse: MindMupResponse): MindMupResponse {
    val expandedMup = expandMinMup(mindMupResponse.ideas)
    val links = mindMupResponse.links + expandedMup.links

    return MindMupResponse(id = mindMupResponse.id, ideas = expandedMup.ideas, links = links)
  }

  private fun expandMinMup(ideas: SortedMap<BigDecimal, Idea>): MindMupResponse {
    val newIdeas = sortedMapOf<BigDecimal, Idea>()
    val newLinks = arrayListOf<Link>()

    for ((index, idea) in ideas) {
      var newIdea = idea

      if (idea.isPattern()) {
        val patternMup = getPatternMup(idea)

        var ideasToBeAdded = getPatternIdeasWithNewIds(idea.getPatternPrefix(), patternMup.ideas)
        if (idea.ideas != null) {
          val expandedMup = expandMinMup(idea.ideas)
          ideasToBeAdded = setEndOfPatternIdeas(ideasToBeAdded, expandedMup.ideas)
          newLinks += expandedMup.links
        }

        newIdea = getIdeaWithNewId(ideasToBeAdded, idea.id)
        newLinks += getPatternLinksWithNewIds(idea, patternMup.links)
      } else if (!idea.hasChildren()) {
        val expandedMup = expandMinMup(idea.ideas!!)
        newIdea = Idea(idea.id, idea.title, idea.attr, expandedMup.ideas)
        newLinks += expandedMup.links
      }
      newIdeas[index] = newIdea
    }

    return MindMupResponse(id = "-1", ideas = newIdeas, links = newLinks)
  }

  private fun getIdeaWithNewId(ideasToBeAdded: SortedMap<BigDecimal, Idea>, id: String): Idea {
    val firstKey = ideasToBeAdded.firstKey()
    val patternRoot = ideasToBeAdded[firstKey]

    return Idea(id, patternRoot!!.title, patternRoot.attr, patternRoot.ideas)
  }

  private fun getPatternMup(idea: Idea): MindMupResponse {
    val patternId = idea.getPatternId() ?: throw PatternIdInvalidException("Missing pattern Id")
    try {
      return mindMupClient.getMup(patternId)
    } catch (exception: GoogleJsonResponseException) {
      throw PatternIdInvalidException("No pattern can be found using id: $patternId")
    }
  }

  private fun getPatternLinksWithNewIds(idea: Idea, links: List<Link>): List<Link> {
    val newLinks = arrayListOf<Link>()
    val patternPrefix = idea.getPatternPrefix()

    for (link in links) {
      val ideaIdFrom = patternPrefix + link.ideaIdFrom
      val ideaIdTo = patternPrefix + link.ideaIdTo
      val newLink = Link(ideaIdFrom, ideaIdTo, link.attr)
      newLinks += newLink
    }

    return newLinks
  }

  private fun getPatternIdeasWithNewIds(
    patternPrefix: String,
    ideas: SortedMap<BigDecimal, Idea>
  ): SortedMap<BigDecimal, Idea> {
    val newIdeas: SortedMap<BigDecimal, Idea> = sortedMapOf()

    for (idea in ideas) {
      val ideaValue = idea.value
      val endIdea = if (ideaValue.ideas.isNullOrEmpty()) {
        sortedMapOf()
      } else {
        getPatternIdeasWithNewIds(patternPrefix, ideaValue.ideas)
      }

      val newIdea = Idea(
        id = patternPrefix + ideaValue.id,
        title = ideaValue.title,
        ideas = endIdea,
        attr = ideaValue.attr
      )
      newIdeas[idea.key] = newIdea
    }
    return newIdeas
  }

  private fun setEndOfPatternIdeas(
    ideas: SortedMap<BigDecimal, Idea>,
    concatIdea: SortedMap<BigDecimal, Idea>
  ): SortedMap<BigDecimal, Idea> {
    val newIdeas: SortedMap<BigDecimal, Idea> = sortedMapOf()

    for (idea in ideas) {
      val ideaValue = idea.value
      val endIdea = if (ideaValue.isPatternEnd()) {
        concatIdea
      } else {
        if (ideaValue.ideas.isNullOrEmpty()) {
          sortedMapOf()
        } else {
          setEndOfPatternIdeas(ideaValue.ideas, concatIdea)
        }
      }
      val newIdea = Idea(
        id = ideaValue.id,
        title = ideaValue.title,
        ideas = endIdea,
        attr = ideaValue.attr
      )
      newIdeas[idea.key] = newIdea
    }
    return newIdeas
  }
}
