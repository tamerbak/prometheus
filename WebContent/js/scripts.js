
function afficher_cacher(id)
{
    if(document.getElementById(id).style.visibility=="hidden")
    {
        document.getElementById(id).style.visibility="visible";
    }
    else
    {
        document.getElementById(id).style.visibility="hidden";
    }
    return true;
}
function selectAll(checkbox) {
    var elements = checkbox.form.elements;
    for (var i = 0; i < elements.length; i++) {
        var element = elements[i];
        if (/_cb$/.test(element.id)) {
            element.checked = checkbox.checked;
        }
    }
}

function terminateopup(xhr, status, args) {  
      
        dlg.hide();  
     
}