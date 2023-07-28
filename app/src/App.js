import './App.css';
import { useState } from 'react';
import OutputView from './OutputView';
import Editor from './Editor';

function displayOutput(result) {
  if (result.success) {
      return result.output.join('\n');
  }
  return result.output;
}

function App() {
  const [result, setResult] = useState({success: false, output: ""});
    return (
        <div id='masterContainer' className='containers'>
            <div id='editorContainer' className='containers'>
                <Editor callback={(data) => {setResult(data);}} />
            </div>
            <div id='outputViewContainer' className='containers'>
                <OutputView success={result.success} output={displayOutput(result)} />
            </div>
        </div>
    );
}

export default App;
