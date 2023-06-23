import './OutputView.css'

export default function OutputView({success, output}) {
    return (
        <>
            <div id='outputText'>Output:</div>
            <textarea 
                id='outputContainer' 
                disabled
                value={output}
                style={{color: success ? '#aed285' : '#ffb4a3'}}></textarea>
        </>
    );
}