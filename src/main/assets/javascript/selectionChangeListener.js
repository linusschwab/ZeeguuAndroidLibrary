document.addEventListener("selectionchange", function() {
    if (window.getSelection().toString().trim() != "")
        Android.updateTranslation(getExtendedSelection());
        //Android.updateText(getExtendedSelection());
    else
        Android.hideTranslation();
}, false);