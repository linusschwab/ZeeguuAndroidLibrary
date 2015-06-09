function extractContribution() {

    var selection = window.getSelection();
    var term = selection.toString();
    var surrounding_paragraph = $(selection.baseNode.parentNode).text();

    // debug information
    console.log(surrounding_paragraph);
    console.log(term);

    var context = extract_context (surrounding_paragraph, term);
    var title = document.getElementsByTagName("title")[0].innerHTML;

    return {
        "term": term,
        "context": context,
        "title": title,
        "url": document.URL
    };
}