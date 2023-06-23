import './Editor.css'

function parseCode(e, callback) {
    e.preventDefault();
    
    // Create a new form to store the code input
    const formData = new FormData();
    const codeInput = document.querySelector("#codeInput");
    formData.append("code", codeInput.value);

    // Create a new request
    const request = new Request(
        'http://127.0.0.1:8080/parse',
        {
          method: 'POST',
          body: formData,
        }
    );
  
    // Fetch data from the server after doing the image classification task
    fetch(request)
    .then((response) => response.json())
    .then((data) => {
        // Run callback on data passed from parent
        callback(data);
    });
}

export default function Editor({callback}) {
    return (
        <form id='editorForm'>
            <div id='toolbar'>
                <button id='parseBtn' className='toolbarBtns' onClick={(e) => parseCode(e, callback)}>Parse</button>
            </div>
            <textarea type='text' id='codeInput'></textarea>
        </form>
    );
}