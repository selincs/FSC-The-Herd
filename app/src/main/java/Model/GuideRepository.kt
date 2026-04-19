package Model

object GuideRepository {

    private val allGuides = listOf(
        // Navigation
        Guide("101", "Finding the Hidden Science Lab Classrooms", "A quick walkthrough from the main quad.", true, false, "Navigation"),
        // Travel
        Guide("102", "FSC Shuttle Bus Schedule", "Times and stops for the campus loop.", false, true, "Travel"),
        // Academic
        Guide("201", "How to Register for Classes", "Step-by-step for the new system.", true, false, "Academic"),
        // Financial Aid
        Guide("202", "FAFSA Deadline Guide", "Important dates for this semester.", true, false, "Financial Aid"),
        // Housing
        Guide("301", "Dorm Room Essentials", "What you can and can't bring.", false, true, "Housing"),
        // Clubs
        Guide("302", "Joining 'The Herd' Club", "How to get involved on campus.", true, false, "Clubs")
    )


    fun getAllGuides(): List<Guide> {
        return allGuides
    }


    fun getGuideById(id: String?): Guide? {
        return allGuides.find { it.id == id }
    }
}